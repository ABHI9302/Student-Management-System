package com.cwm.studentmanagement.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cwm.studentmanagement.model.Courses;

@Repository // FIXED: Explicitly added stereotype annotation for clean Spring component scan resolution
public interface CourseRepository extends JpaRepository<Courses, Long> {

	// Verifies if a course code exists ignoring text casing layout patterns
	boolean existsByCourseCodeIgnoreCase(String code);

	// Verifies uniqueness constraints during update operations across records
	boolean existsByCourseCodeIgnoreCaseAndIdNot(String code, Long id);

	// Fetches active courses wrapping data in paginated page metrics safely
	Page<Courses> findByActiveTrue(Pageable pageable);

	// FIXED: Standardizes custom list selection with programmatic sorting architecture
	List<Courses> findByActiveTrue(Sort sort);
}
