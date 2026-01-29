package com.example.anonymous_board.service;

import com.example.anonymous_board.domain.BoardType;
import com.example.anonymous_board.domain.Member;
import com.example.anonymous_board.domain.Post;
import com.example.anonymous_board.domain.PostImage;
import com.example.anonymous_board.domain.Poll;
import com.example.anonymous_board.domain.PollOption;
import com.example.anonymous_board.domain.PollVote;
import com.example.anonymous_board.domain.Vote;
import com.example.anonymous_board.domain.VoteType;
import com.example.anonymous_board.dto.MyPostResponse;
import com.example.anonymous_board.dto.PostCreateRequest;
import com.example.anonymous_board.dto.PostResponse;
import com.example.anonymous_board.dto.PostUpdateRequest;
import com.example.anonymous_board.repository.PollOptionRepository;
import com.example.anonymous_board.repository.PollVoteRepository;
import com.example.anonymous_board.repository.PostRepository;
import com.example.anonymous_board.repository.UserRepository;
import com.example.anonymous_board.repository.VoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final VoteRepository voteRepository;
    private final PollOptionRepository pollOptionRepository;
    private final PollVoteRepository pollVoteRepository;
    private final FileStorageService fileStorageService;
    private final HotPostsCacheService hotPostsCacheService;
    private final UserRepository userRepository;

    // 투표
    @Transactional
    public void votePoll(Long pollOptionId, Member member) {
        PollOption option = pollOptionRepository.findById(pollOptionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 투표 항목입니다."));

        Poll poll = option.getPoll();

        Optional<PollVote> existingVote = pollVoteRepository.findByPollAndMember(poll, member);

        if (existingVote.isPresent()) {
            PollVote vote = existingVote.get();
            if (vote.getOption().getId().equals(pollOptionId)) {
                return; // 이미 같은 항목에 투표함
            }
            // 기존 투표 취소 (카운트 감소)
            PollOption oldOption = vote.getOption();
            oldOption.setVoteCount(oldOption.getVoteCount() - 1);

            // 새 항목 투표
            vote.setOption(option);
            option.setVoteCount(option.getVoteCount() + 1);
        } else {
            // 새 투표
            PollVote vote = new PollVote(poll, member, option);
            pollVoteRepository.save(vote);
            option.setVoteCount(option.getVoteCount() + 1);
        }
    }

    // 1. 게시글 생성
    @Transactional
    public PostResponse createPost(PostCreateRequest request, List<MultipartFile> files, Member currentUser) {
        // 데이터베이스에서 최신 사용자 정보 조회 (닉네임 변경 반영)
        Member latestUser = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Post post = new Post();
        post.setNickname(latestUser.getNickname()); // 최신 닉네임 사용
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setMember(latestUser); // 실제 작성자 설정

        // 공지사항 설정 (관리자만 가능)
        if (request.getIsAnnouncement() != null && request.getIsAnnouncement()) {
            // 관리자 권한 확인
            if (!latestUser.getRole().getKey().equals("ROLE_ADMIN")) {
                throw new IllegalArgumentException("공지사항은 관리자만 작성할 수 있습니다.");
            }
            post.setAnnouncement(true);
        }

        // boardType 설정 (요청에서 받은 값을 enum으로 변환, 기본값은 FREE)
        try {
            BoardType boardType = BoardType.valueOf(request.getBoardType().toUpperCase());
            post.setBoardType(boardType);
        } catch (Exception e) {
            post.setBoardType(BoardType.FREE); // 잘못된 값이면 기본값 FREE
        }

        // 게시글 저장
        Post savedPost = postRepository.save(post);

        // 투표 생성
        if (request.getPollQuestion() != null && !request.getPollQuestion().isBlank() &&
                request.getPollOptions() != null && request.getPollOptions().size() >= 2) {

            Poll poll = new Poll(request.getPollQuestion(), savedPost);
            for (String optionText : request.getPollOptions()) {
                if (optionText != null && !optionText.isBlank()) {
                    poll.addOption(new PollOption(optionText));
                }
            }
            savedPost.setPoll(poll);
            postRepository.save(savedPost); // Poll 저장을 위해 다시 저장 (Cascade)
        }

        // 이미지 저장
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    try {
                        String imageUrl = fileStorageService.storeFile(file, "posts");
                        String filename = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
                        String finalPath = "/posts/images/" + filename;
                        savedPost.addImage(new PostImage(finalPath, savedPost));
                    } catch (java.io.IOException e) {
                        throw new RuntimeException("이미지 저장 실패", e);
                    }
                }
            }
        }

        return new PostResponse(savedPost, currentUser);
    }

    // 2. 게시글 전체 조회
    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    // 2-1. 게시글 전체 조회 (정렬 및 페이지네이션)
    public Page<Post> getAllPosts(int page, int size, String sortBy) {
        // 공지사항 조회
        List<Post> announcements = postRepository.findAnnouncements();

        Pageable pageable;

        // 첫 페이지에서 공지사항을 포함하는 경우, 일반 게시글 사이즈 조정
        if (page == 0 && !announcements.isEmpty()) {
            int adjustedSize = Math.max(1, size - announcements.size());
            pageable = createPageable(page, adjustedSize, sortBy);
        } else {
            pageable = createPageable(page, size, sortBy);
        }

        Page<Post> regularPosts = postRepository.findNonAnnouncementPosts(pageable);

        // 첫 페이지에만 공지사항을 포함
        if (page == 0 && !announcements.isEmpty()) {
            List<Post> combinedPosts = new java.util.ArrayList<>(announcements);
            combinedPosts.addAll(regularPosts.getContent());

            // 공지사항을 포함한 총 개수 계산
            long totalElements = announcements.size() + regularPosts.getTotalElements();

            return new org.springframework.data.domain.PageImpl<>(
                    combinedPosts,
                    PageRequest.of(page, size),
                    totalElements);
        }

        return regularPosts;
    }

    // 2-2. 게시판 타입별 게시글 조회 (정렬 및 페이지네이션)
    public Page<Post> getAllPostsByBoardType(int page, int size, String sortBy, BoardType boardType) {
        // 해당 게시판의 공지사항 조회
        List<Post> announcements = postRepository.findAnnouncementsByBoardType(boardType);

        Pageable pageable;

        // 첫 페이지에서 공지사항을 포함하는 경우, 일반 게시글 사이즈 조정
        if (page == 0 && !announcements.isEmpty()) {
            int adjustedSize = Math.max(1, size - announcements.size());
            pageable = createPageable(page, adjustedSize, sortBy);
        } else {
            pageable = createPageable(page, size, sortBy);
        }

        Page<Post> regularPosts = postRepository.findNonAnnouncementPostsByBoardType(boardType, pageable);

        // 첫 페이지에만 공지사항을 포함
        if (page == 0 && !announcements.isEmpty()) {
            List<Post> combinedPosts = new java.util.ArrayList<>(announcements);
            combinedPosts.addAll(regularPosts.getContent());

            // 공지사항을 포함한 총 개수 계산
            long totalElements = announcements.size() + regularPosts.getTotalElements();

            return new org.springframework.data.domain.PageImpl<>(
                    combinedPosts,
                    PageRequest.of(page, size),
                    totalElements);
        }

        return regularPosts;
    }

    private Pageable createPageable(int page, int size, String sortBy) {
        switch (sortBy) {
            case "likes":
                return PageRequest.of(page, size, Sort.by("likes").descending());
            case "dislikes":
                return PageRequest.of(page, size, Sort.by("dislikes").descending());
            case "viewCount":
                return PageRequest.of(page, size, Sort.by("viewCount").descending());
            case "comments":
                return PageRequest.of(page, size, Sort.by("commentCount").descending());
            case "latest":
            default:
                return PageRequest.of(page, size, Sort.by("createdAt").descending());
        }
    }

    // 3. 게시글 단건 조회
    @Transactional(readOnly = true)
    public Post getPostById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
    }

    // 4. 조회수 증가
    @Transactional
    public void incrementViewCount(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        post.incrementViewCount();
    }

    // 5. 게시글 수정
    @Transactional
    public void updatePost(Long id, PostUpdateRequest request, List<MultipartFile> imageFiles, Member currentUser) {
        Post post = getPostById(id);

        // 본인 확인 (작성자만 수정 가능)
        if (!post.getMember().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("본인이 작성한 게시글만 수정할 수 있습니다.");
        }

        post.update(request.getTitle(), request.getContent());

        // 1. 기존 이미지 삭제 (요청된 ID에 해당하는 이미지만 삭제)
        if (request.getDeleteImageIds() != null && !request.getDeleteImageIds().isEmpty()) {
            for (Long imageId : request.getDeleteImageIds()) {
                post.removeImage(imageId);
            }
        }

        // 2. 새로운 이미지 추가
        if (imageFiles != null && !imageFiles.isEmpty()) {
            for (MultipartFile imageFile : imageFiles) {
                if (!imageFile.isEmpty()) {
                    try {
                        String imageUrl = fileStorageService.storeFile(imageFile, "posts");
                        String filename = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
                        String finalPath = "/posts/images/" + filename;
                        post.addImage(new PostImage(finalPath, post));
                    } catch (java.io.IOException e) {
                        throw new RuntimeException("이미지 업로드 중 오류가 발생했습니다.", e);
                    }
                }
            }
        }
    }

    // 6. 게시글 삭제
    @Transactional
    public void deletePost(Long id, Member currentUser) {
        Post post = getPostById(id);

        // 본인 확인 (작성자만 삭제 가능)
        if (!post.getMember().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("본인이 작성한 게시글만 삭제할 수 있습니다.");
        }

        postRepository.delete(post);
    }

    // 7. 추천/비추천
    @Transactional
    public void vote(Long postId, Member currentUser, VoteType voteType) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        if (post.getMember().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("자신의 글에는 추천/비추천할 수 없습니다.");
        }

        Optional<Vote> existingVote = voteRepository.findByMemberAndPost(currentUser, post);

        if (existingVote.isPresent()) {
            Vote vote = existingVote.get();
            // 이미 같은 타입으로 투표했다면, 투표 취소
            if (vote.getVoteType() == voteType) {
                if (voteType == VoteType.LIKE) {
                    post.decrementLikes();
                } else {
                    post.decrementDislikes();
                }
                voteRepository.delete(vote);
            } else { // 다른 타입으로 투표했다면, 투표 변경
                if (voteType == VoteType.LIKE) {
                    post.decrementDislikes();
                    post.incrementLikes();
                } else {
                    post.incrementDislikes();
                    post.decrementLikes();
                }
                vote.setVoteType(voteType);
                voteRepository.save(vote);
            }
        } else { // 첫 투표
            if (voteType == VoteType.LIKE) {
                post.incrementLikes();
            } else {
                post.incrementDislikes();
            }
            voteRepository.save(new Vote(currentUser, post, voteType));
        }

        // 추천수 변경 시 핸 게시글 캐시 무효화
        hotPostsCacheService.invalidateCache();
    }

    // 8. 게시글 검색 (대소문자 구분 없음, 페이지네이션 지원)
    public Page<Post> searchPosts(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        // 검색된 공지사항과 일반 게시글을 분리하여 조회
        List<Post> announcements = postRepository.findAnnouncementsByKeyword(keyword);
        Page<Post> regularPosts = postRepository.findNonAnnouncementsByKeyword(keyword, pageable);

        // 첫 페이지에만 공지사항을 포함
        if (page == 0 && !announcements.isEmpty()) {
            List<Post> combinedPosts = new java.util.ArrayList<>(announcements);
            combinedPosts.addAll(regularPosts.getContent());

            long totalElements = announcements.size() + regularPosts.getTotalElements();

            return new org.springframework.data.domain.PageImpl<>(
                    combinedPosts,
                    pageable,
                    totalElements);
        }

        return regularPosts;
    }

    // 9. 핫 게시글 조회 (추천수 또는 비추천수 10 이상)
    public Page<Post> getHotPosts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return postRepository.findHotPosts(10, pageable);
    }

    public List<Post> getPostsByMember(Member member) {
        return postRepository.findByMemberOrderByCreatedAtDesc(member);
    }

    // 10. 내가 작성한 게시글 조회 (프로필용)
    public List<MyPostResponse> getMyPosts(Member member) {
        List<Post> posts = postRepository.findByMemberOrderByCreatedAtDesc(member);
        return posts.stream()
                .map(post -> MyPostResponse.builder()
                        .id(post.getId())
                        .title(post.getTitle())
                        .content(post.getContent().length() > 100
                                ? post.getContent().substring(0, 100) + "..."
                                : post.getContent())
                        .viewCount(post.getViewCount())
                        .commentCount(post.getCommentCount())
                        .createdAt(post.getCreatedAt())
                        .build())
                .collect(java.util.stream.Collectors.toList());
    }

    // 11. 내가 작성한 게시글 조회 (페이지네이션)
    public Page<MyPostResponse> getMyPostsPaged(Member member, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Post> postPage = postRepository.findByMember(member, pageable);

        return postPage.map(post -> MyPostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent().length() > 100
                        ? post.getContent().substring(0, 100) + "..."
                        : post.getContent())
                .viewCount(post.getViewCount())
                .commentCount(post.getCommentCount())
                .createdAt(post.getCreatedAt())
                .build());
    }
}
