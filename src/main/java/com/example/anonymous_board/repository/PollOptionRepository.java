package com.example.anonymous_board.repository;

import com.example.anonymous_board.domain.PollOption;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PollOptionRepository extends JpaRepository<PollOption, Long> {

}
