package com.cwm.studentmanagement.service;

import java.util.List;

import org.springframework.data.domain.Page;

import com.cwm.studentmanagement.dto.EnrollmentDTO;
import com.cwm.studentmanagement.dto.EnrollmentSummaryDTO;


public interface EnrollmentService {
	
	void enrollStudentToCourses(EnrollmentDTO enrollmentDTO);
	
	Page<EnrollmentSummaryDTO> getEnrolledStudents(int page, int size);
	
	EnrollmentSummaryDTO findEnrolledStudentCourseDetails(Long studentId);
	
	List<EnrollmentSummaryDTO> getRecentlyEnrolledStudents();

}
