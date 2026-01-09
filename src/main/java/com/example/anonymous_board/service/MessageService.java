package com.example.anonymous_board.service;

import com.example.anonymous_board.domain.Member;
import com.example.anonymous_board.domain.Message;
import com.example.anonymous_board.domain.UserConversation;
import com.example.anonymous_board.dto.ConversationDto;
import com.example.anonymous_board.dto.ConversationSummaryDto;
import com.example.anonymous_board.dto.MessageCreateRequest;
import com.example.anonymous_board.dto.MessageDto;
import com.example.anonymous_board.repository.MessageRepository;
import com.example.anonymous_board.repository.UserConversationRepository;
import com.example.anonymous_board.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MessageService {

        private final MessageRepository messageRepository;
        private final UserRepository userRepository;
        private final UserConversationRepository userConversationRepository;

        // 대화 찾기
        public ConversationDto getConversation(Long user1Id, Long user2Id) {
                Member user1 = userRepository.findById(user1Id)
                                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
                Member user2 = userRepository.findById(user2Id)
                                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

                List<Message> messages = messageRepository.findConversation(user1, user2);
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
}
