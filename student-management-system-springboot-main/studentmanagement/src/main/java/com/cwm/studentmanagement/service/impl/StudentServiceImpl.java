package com.cwm.studentmanagement.service.impl;

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

import com.cwm.studentmanagement.dto.StudentDTO;
import com.cwm.studentmanagement.model.Students;
import com.cwm.studentmanagement.repository.StudentRepository;
import com.cwm.studentmanagement.service.StudentService;

@Service
@Transactional
public class StudentServiceImpl implements StudentService {

	private static final Logger log = LoggerFactory.getLogger(StudentServiceImpl.class);

	private final StudentRepository studentRepository;
	private final ModelMapper mapper;

	public StudentServiceImpl(StudentRepository studentRepository, ModelMapper mapper) {
		this.studentRepository = studentRepository;
		this.mapper = mapper;
	}

	@Override
	@Transactional(readOnly = true) // FIXED: Added read-only transaction mode for fast lookups
	public boolean existsByEmailIgnoreCase(String email) {
		log.info("Checking unique email registration state for: {}", email);
		return studentRepository.existsByEmailIgnoreCase(email);
	}

	@Override
	public StudentDTO createStudent(StudentDTO studentDTO) {
		log.info("Processing persistence storage mapping for new student registration record.");

		Students studentEntity = mapper.map(studentDTO, Students.class);
		Students saved = studentRepository.save(studentEntity);

		return mapper.map(saved, StudentDTO.class);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<StudentDTO> getStudents(int page, int size) {
		log.info("Fetching paginated active students catalog from index: {}", page);

		PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Direction.DESC, "id"));
		return studentRepository.findByActiveTrue(pageRequest)
				.map(student -> mapper.map(student, StudentDTO.class));
	}

	@Override
	@Transactional(readOnly = true)
	public StudentDTO getStudentById(Long id) {
		log.info("Fetching profile details data matrix for student ID: {}", id);

		Students student = studentRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("No student found with identity ID: " + id));

		return mapper.map(student, StudentDTO.class);
	}

	@Override
	@Transactional(readOnly = true)
	public boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id) {
		log.info("Verifying email uniqueness constraints inside modification scope for ID: {}", id);
		return studentRepository.existsByEmailIgnoreCaseAndIdNot(email, id);
	}

	@Override
	public StudentDTO updateStudent(Long id, StudentDTO studentDTO) {
		log.info("Executing transaction pipeline updates for student ID: {}", id);

		Students student = studentRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("No student found with identity ID: " + id));

		// FIXED: Captured core immutable properties to block accidental null reference overrides
		Long originalId = student.getId();
		java.time.LocalDateTime originalCreatedAt = student.getCreatedAt();

		mapper.map(studentDTO, student);

		// Re-assign protected properties securely before triggering engine dirty flushes
		student.setId(originalId);
		student.setCreatedAt(originalCreatedAt);

		Students updated = studentRepository.save(student);
		return mapper.map(updated, StudentDTO.class);
	}

	@Override
	@Transactional(readOnly = true)
	public List<StudentDTO> getAllStudents() {
		log.info("Fetching raw active student records list for selection dropdown forms.");

		return studentRepository.findByActiveTrue().stream()
				.map(student -> mapper.map(student, StudentDTO.class))
				.collect(Collectors.toList());
	}
}
