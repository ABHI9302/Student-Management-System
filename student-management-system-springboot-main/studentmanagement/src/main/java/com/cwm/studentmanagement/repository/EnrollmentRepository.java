package com.cwm.studentmanagement.repository;

import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.cwm.studentmanagement.model.Enrollment;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

	boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);

	@Query("""
			select count(distinct e.student.id) 
			from Enrollment e 
			where e.enrolledDate between :startDate and :endDate
			""")
	long countDistinctStudentByEnrollDateBetween(@Param("startDate") LocalDateTime startDate,
												 @Param("endDate") LocalDateTime endDate);

	// Name of the course that has the most enrollments (null when there are no enrollments yet)
	@Query("""
			select c.courseName
			from Enrollment e
			join e.course c
			group by c.courseName
			order by count(e.id) desc
			limit 1
			""")
	String findTopPerformingCourseName();
}
