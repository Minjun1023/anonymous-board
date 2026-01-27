package com.example.anonymous_board.service;

import com.example.anonymous_board.domain.Member;
import com.example.anonymous_board.domain.Message;
import com.example.anonymous_board.domain.UserConversation;
import com.example.anonymous_board.dto.ConversationDto;
import com.example.anonymous_board.dto.ConversationSummaryDto;
import com.example.anonymous_board.dto.MessageCreateRequest;
import com.example.anonymous_board.dto.MessageDto;
import com.example.anonymous_board.dto.ReadStatusDto;
import com.example.anonymous_board.repository.MessageRepository;
import com.example.anonymous_board.repository.UserConversationRepository;
import com.example.anonymous_board.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MessageService {

        private final MessageRepository messageRepository;
        private final UserRepository userRepository;
        private final UserConversationRepository userConversationRepository;
        private final RedisTemplate<String, Object> redisTemplate;

        // 대화 찾기
        @Transactional
        public ConversationDto getConversation(Long user1Id, Long user2Id) {
                log.info("========== getConversation 호출 ==========");
                log.info("user1Id (요청자): {}", user1Id);
                log.info("user2Id (상대방): {}", user2Id);

                Member user1 = userRepository.findById(user1Id)
                                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
                Member user2 = userRepository.findById(user2Id)
                                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

                List<Message> messages = messageRepository.findConversation(user1, user2);
                log.info("전체 메시지 개수: {}", messages.size());

                // user1이 받은 메시지 중 읽지 않은 메시지 수집 및 읽음 처리
                List<Long> readMessageIds = new ArrayList<>();
                messages.stream()
                                .filter(msg -> msg.getReceiver().getId().equals(user1Id))
                                .filter(msg -> msg.getReadAt() == null)
                                .forEach(msg -> {
                                        msg.markAsRead();
                                        readMessageIds.add(msg.getId());
                                });

                // 읽음 처리한 메시지가 있으면 Redis로 이벤트 발행
                if (!readMessageIds.isEmpty()) {
                        log.info("========== 읽음 상태 이벤트 발행 ==========");
                        log.info("읽은 메시지 개수: {}", readMessageIds.size());
                        log.info("메시지 ID 목록: {}", readMessageIds);
                        log.info("발신자 ID (알림 받을 사람): {}", user2Id);
                        log.info("수신자 ID (읽은 사람): {}", user1Id);

                        ReadStatusDto readStatus = new ReadStatusDto();
                        readStatus.setSenderId(user2Id); // 발신자에게 알림
                        readStatus.setReceiverId(user1Id); // 읽은 사람
                        readStatus.setMessageIds(readMessageIds);

                        redisTemplate.convertAndSend("chat", readStatus);
                        log.info("✅ Redis로 읽음 상태 발행 완료");
                } else {
                        log.info("읽을 메시지 없음 (모두 이미 읽음)");
                }

                List<MessageDto> messageDtos = messages.stream()
                                .map(MessageDto::from)
                                .collect(Collectors.toList());

                boolean otherUserHasLeft = userConversationRepository.findByUserAndOtherUser(user2, user1)
                                .map(UserConversation::isHidden)
                                .orElse(false);

                return ConversationDto.builder()
                                .messages(messageDtos)
                                .otherUserHasLeft(otherUserHasLeft)
                                .build();
        }

        // 메시지 보내기
        @Transactional
        public MessageDto sendMessage(Long senderId, MessageCreateRequest request) {
                Member sender = userRepository.findById(senderId)
                                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
                Member receiver = userRepository.findById(request.getReceiverId())
                                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

                Message message = Message.builder()
                                .sender(sender)
                                .receiver(receiver)
                                .content(request.getContent())
                                .build();

                Message savedMessage = messageRepository.save(message);

                unhideConversation(sender, receiver);
                unhideConversation(receiver, sender);

                return MessageDto.from(savedMessage);
        }

        // 대화 목록 찾기
        public List<ConversationSummaryDto> getConversationSummaries(Long userId) {
                Member currentUser = userRepository.findById(userId)
                                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

                List<Message> allMessages = messageRepository.findBySenderOrReceiverOrderByCreatedAtDesc(currentUser,
                                currentUser);

                Map<Member, List<Message>> conversations = allMessages.stream()
                                .collect(Collectors.groupingBy(message -> {
                                        if (message.getSender().equals(currentUser)) {
                                                return message.getReceiver();
                                        } else {
                                                return message.getSender();
                                        }
                                }));

                return conversations.entrySet().stream()
                                .filter(entry -> {
                                        Optional<UserConversation> userConversation = userConversationRepository
                                                        .findByUserAndOtherUser(currentUser, entry.getKey());
                                        return userConversation.map(uc -> !uc.isHidden()).orElse(true);
                                })
                                .map(entry -> {
                                        Member otherUser = entry.getKey();
                                        List<Message> messages = entry.getValue();
                                        Message lastMessage = messages.stream()
                                                        .max(Comparator.comparing(Message::getCreatedAt))
                                                        .orElse(null);

                                        String profileImage = otherUser.getProfileImage();
                                        if (profileImage != null && !profileImage.startsWith("/profiles/")
                                                        && !profileImage.startsWith("http")) {
                                                profileImage = "/profiles/" + profileImage;
                                        }

                                        return ConversationSummaryDto.builder()
                                                        .otherUserId(otherUser.getId())
                                                        .otherUserNickname(otherUser.getNickname())
                                                        .profileImage(profileImage)
                                                        .lastMessageContent(
                                                                        lastMessage != null ? lastMessage.getContent()
                                                                                        : "대화 없음")
                                                        .lastMessageTime(
                                                                        lastMessage != null ? lastMessage.getCreatedAt()
                                                                                        : null)
                                                        .hasUnreadMessages(false)
                                                        .build();
                                })
                                .sorted(Comparator.comparing(ConversationSummaryDto::getLastMessageTime,
                                                Comparator.nullsLast(Comparator.reverseOrder())))
                                .collect(Collectors.toList());
        }

        // 대화 나가기
        @Transactional
        public void leaveChat(Long userId, Long otherUserId) {
                Member user = userRepository.findById(userId)
                                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
                Member otherUser = userRepository.findById(otherUserId)
                                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

                UserConversation userConversation = userConversationRepository.findByUserAndOtherUser(user, otherUser)
                                .orElseGet(() -> UserConversation.builder().user(user).otherUser(otherUser).build());

                userConversation.hide();
                userConversationRepository.save(userConversation);
        }

        // 대화 복구
        private void unhideConversation(Member user, Member otherUser) {
                userConversationRepository.findByUserAndOtherUser(user, otherUser)
                                .ifPresent(conversation -> {
                                        if (conversation.isHidden()) {
                                                conversation.unhide();
                                                userConversationRepository.save(conversation);
                                        }
                                });
        }

        // 개별 메시지 읽음 처리
        @Transactional
        public void markMessageAsRead(Long messageId, Long userId) {
                Message message = messageRepository.findById(messageId)
                                .orElseThrow(() -> new IllegalArgumentException("메시지를 찾을 수 없습니다."));

                // 수신자만 읽음 처리 가능
                if (!message.getReceiver().getId().equals(userId)) {
                        throw new IllegalArgumentException("수신자만 읽음 처리할 수 있습니다.");
                }

                // 이미 읽은 메시지는 처리하지 않음
                if (message.getReadAt() != null) {
                        log.info("이미 읽은 메시지: messageId={}", messageId);
                        return;
                }

                // 읽음 처리
                message.markAsRead();
                log.info("메시지 읽음 처리: messageId={}", messageId);

                // Redis로 읽음 상태 이벤트 발행
                ReadStatusDto readStatus = new ReadStatusDto();
                readStatus.setSenderId(message.getSender().getId()); // 발신자에게 알림
                readStatus.setReceiverId(userId); // 읽은 사람
                readStatus.setMessageIds(List.of(messageId));

                redisTemplate.convertAndSend("chat", readStatus);
                log.info("✅ 읽음 상태 이벤트 발행: messageId={}, senderId={}", messageId, message.getSender().getId());
        }
}
