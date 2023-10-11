package com.cst438.service;

import com.cst438.domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@Service
@ConditionalOnProperty(prefix = "gradebook", name = "service", havingValue = "rest")
@RestController
public class GradebookServiceREST implements GradebookService {

	private RestTemplate restTemplate = new RestTemplate();

	@Value("${gradebook.url}")
	private static String gradebook_url;

	@Override
	public void enrollStudent(String student_email, String student_name, int course_id) {
		System.out.println("Start Message "+ student_email +" " + course_id); 

		Enrollment enrollment = enrollmentRepository.findByEmailAndCourseId(student_email, course_id);
		EnrollmentDTO enrollmentDTO = new EnrollmentDTO(enrollment.getEnrollment_id(), student_email, student_name, course_id);
		EnrollmentDTO response = restTemplate.postForObject(
				"http://localhost:8081/enrollment",
				enrollmentDTO,
				EnrollmentDTO.class,
				gradebook_url);
	}
	
	@Autowired
	EnrollmentRepository enrollmentRepository;
	/*
	 * endpoint for final course grades
	 */
	@PutMapping("/course/{course_id}")
	@Transactional
	public void updateCourseGrades( @RequestBody FinalGradeDTO[] grades, @PathVariable("course_id") int course_id) {
		System.out.println("Grades received "+grades.length);

		for (int i = 0; i < grades.length; i++) {
			Enrollment studentEnrollment = enrollmentRepository.findByEmailAndCourseId(grades[i].studentEmail(), grades[i].courseId());
			studentEnrollment.setCourseGrade(grades[i].grade());
			enrollmentRepository.save(studentEnrollment);
		}
	}
}
