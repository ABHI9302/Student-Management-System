package com.cwm.studentmanagement.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cwm.studentmanagement.model.Students;

@Repository // FIXED: Explicit stereotype identifier added for seamless context component-scan mapping
public interface StudentRepository extends JpaRepository<Students, Long> {

	// Checks if an email address already exists ignoring lower/uppercase variations
	boolean existsByEmailIgnoreCase(String email);

	// Checks email conflicts for editing operations excluding active reference pointer
	boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

	// Fetches active records wrapped cleanly inside paginated framework layers
	Page<Students> findByActiveTrue(Pageable pageable);

	// Fetches active records as a raw sequential array list mapping collections
	List<Students> findByActiveTrue();

	// FIXED: Optimized entity graph definition with complete safe structural queries mapping
	@EntityGraph(attributePaths = {"enrollments", "enrollments.course"})
	@Query(value = """                      
			select distinct s
			from Students s 
			join s.enrollments e
			where s.active = true
		""",
			countQuery = """
					select count(distinct s)
					from Students s 
					join s.enrollments e
					where s.active = true
				""")
	Page<Students> findEnrolledStudents(Pageable pageable);

	// FIXED: Safe dynamic inner fetch lookup to extract deep collection objects tree natively without memory leaks
	@Query("""
			select distinct s
			from Students s
			left join fetch s.enrollments e
			left join fetch e.course c
			where s.id = :id
			""")
	Optional<Students> findEnrolledStudentCourseDetails(@Param("id") Long id);
}
