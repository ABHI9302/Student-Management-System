package com.cwm.studentmanagement.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cwm.studentmanagement.dto.CourseDTO;
import com.cwm.studentmanagement.dto.EnrollmentDTO;
import com.cwm.studentmanagement.dto.EnrollmentSummaryDTO;
import com.cwm.studentmanagement.model.Courses;
import com.cwm.studentmanagement.model.Enrollment;
import com.cwm.studentmanagement.model.Students;
import com.cwm.studentmanagement.repository.CourseRepository;
import com.cwm.studentmanagement.repository.EnrollmentRepository;
import com.cwm.studentmanagement.repository.StudentRepository;
import com.cwm.studentmanagement.service.EnrollmentService;

@Service
@Transactional
public class EnrollmentServiceImpl implements EnrollmentService {
	private static final Logger log = LoggerFactory.getLogger(EnrollmentServiceImpl.class);

	private final EnrollmentRepository enrollmentRepository;
	private final StudentRepository studentRepository;
	private final CourseRepository courseRepository;
	private final ModelMapper mapper;

	public EnrollmentServiceImpl(EnrollmentRepository enrollmentRepository,
								 StudentRepository studentRepository,
								 CourseRepository courseRepository,
								 ModelMapper mapper)
	{
		this.enrollmentRepository = enrollmentRepository;
		this.studentRepository = studentRepository;
		this.courseRepository = courseRepository;
		this.mapper = mapper;
	}

	@Override
	public void enrollStudentToCourses(EnrollmentDTO enrollmentDTO) {
		log.info("Processing multiple course enrollment request for Student ID: {}", enrollmentDTO.getStudentId());

		Students student = studentRepository.findById(enrollmentDTO.getStudentId())
				.orElseThrow(() -> new RuntimeException("Student not found"));

		for(Long courseId : enrollmentDTO.getCourseIds()) {
			Courses course = courseRepository.findById(courseId)
					.orElseThrow(() -> new RuntimeException("Course not found"));

			if(enrollmentRepository.existsByStudentIdAndCourseId(enrollmentDTO.getStudentId(), courseId)) {
				log.warn("Student already registered for Course ID: {}. Skipping duplicate.", courseId);
				continue;
			}

			Enrollment enrollment = new Enrollment();
			enrollment.setStudent(student);
			enrollment.setCourse(course);

			student.getEnrollments().add(enrollment);
			course.getEnrollments().add(enrollment);

			enrollmentRepository.save(enrollment);
		}
	}

	@Override
	@Transactional(readOnly = true)
	public Page<EnrollmentSummaryDTO> getEnrolledStudents(int page, int size) {
		log.info("Fetching enrolled summary overview records for page index: {}", page);

		PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Direction.DESC, "id"));
		return studentRepository.findEnrolledStudents(pageRequest)
				.map(student -> {
					EnrollmentSummaryDTO dto = new EnrollmentSummaryDTO();
					dto.setStudentId(student.getId());
					dto.setStudentName(student.getFirstName() + " " + student.getLastName());
					dto.setEmail(student.getEmail());

					dto.setCourseCount(student.getEnrollments().size());
					BigDecimal totalFee = student.getEnrollments().stream()
							.map(enrollment -> enrollment.getCourse().getFee())
							.filter(fee -> fee != null)
							.reduce(BigDecimal.ZERO, BigDecimal::add);
					dto.setTotalFee(totalFee);

					return dto;
				});
	}

	@Override
	@Transactional(readOnly = true)
	public EnrollmentSummaryDTO findEnrolledStudentCourseDetails(Long studentId) {
		log.info("Compiling detailed relational records tree for Student ID: {}", studentId);

		return studentRepository.findEnrolledStudentCourseDetails(studentId)
				.map(student -> {
					EnrollmentSummaryDTO dto = new EnrollmentSummaryDTO();
					dto.setStudentId(student.getId());
					dto.setStudentName(student.getFirstName() + " " + student.getLastName());
					dto.setEmail(student.getEmail());

					dto.setCourseCount(student.getEnrollments().size());
					BigDecimal totalFee = student.getEnrollments().stream()
							.map(enrollment -> enrollment.getCourse().getFee())
							.filter(fee -> fee != null)
							.reduce(BigDecimal.ZERO, BigDecimal::add);
					dto.setTotalFee(totalFee);

					List<CourseDTO> courseList = student.getEnrollments().stream()
							.map(enrollment -> mapper.map(enrollment.getCourse(), CourseDTO.class))
							.collect(Collectors.toList());
					dto.setCourseList(courseList);

					return dto;
				})
				.orElseThrow(() -> new RuntimeException("No active enrollment tracking found for Student ID: " + studentId));
	}

	// FIXED & ADDED: This missing method resolves your compile error completely
	@Override
	@Transactional(readOnly = true)
	public List<EnrollmentSummaryDTO> getRecentlyEnrolledStudents() {
		log.info("Fetching top 5 recently enrolled students for dashboard.");

		// PageRequest to fetch the first 5 records sorted by primary key descending
		PageRequest limitRequest = PageRequest.of(0, 5, Sort.by(Direction.DESC, "id"));

		return studentRepository.findEnrolledStudents(limitRequest).stream()
				.map(student -> {
					EnrollmentSummaryDTO dto = new EnrollmentSummaryDTO();
					dto.setStudentId(student.getId());
					dto.setStudentName(student.getFirstName() + " " + student.getLastName());
					dto.setEmail(student.getEmail());
					dto.setCourseCount(student.getEnrollments().size());

					BigDecimal totalFee = student.getEnrollments().stream()
							.map(enrollment -> enrollment.getCourse().getFee())
							.filter(fee -> fee != null)
							.reduce(BigDecimal.ZERO, BigDecimal::add);
					dto.setTotalFee(totalFee);

					return dto;
				})
				.collect(Collectors.toList());
	}
}
