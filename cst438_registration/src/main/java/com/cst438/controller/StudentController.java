package com.cst438.controller;


import com.cst438.domain.Enrollment;
import com.cst438.domain.EnrollmentRepository;
import com.cst438.domain.Student;
import com.cst438.domain.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@RestController
public class StudentController {
    @Autowired
    StudentRepository studentRepository;

    @Autowired
    EnrollmentRepository enrollmentRepository;

    @GetMapping("/student/list")
    public Iterable<Student> listStudents() {
        return studentRepository.findAll();
    }

    @PostMapping("/student/add")
    public Student addStudent(@RequestBody Student s) {
        Student student = studentRepository.findByEmail(s.getEmail());
        // Can successfully add a student record when the email is distinct
        if (student == null) {
            student = new Student();
            student.setName(s.getName());
            student.setEmail(s.getEmail());
            student.setStatusCode(s.getStatusCode());
            student.setStatus(s.getStatus());
            studentRepository.save(s);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "A student with that email already exists.");
        }
        return student;
    }

    @PutMapping("/student/update")
    public Student update(@RequestBody Student us) {
        Student student = studentRepository.findById(us.getStudent_id()).orElse(null);
        if(student != null) {
            student.setStudent_id(us.getStudent_id());
            student.setName(us.getName());
            student.setEmail(us.getEmail());
            student.setStatusCode(us.getStatusCode());
            student.setStatus(us.getStatus());
            return studentRepository.save(student);
        }
        return null;
    }


    @DeleteMapping("/student/delete")
    public void delete(
            @RequestParam("id") Integer id,
            @RequestParam("FORCE")Optional<Boolean> FORCE) {
        Enrollment[] studentInEnrollment = enrollmentRepository.findStudentInEnrollment(id);
        Student student =  studentRepository.findById(id).orElse(null);

        if(student != null) {
            if (studentInEnrollment.length == 0) {
                studentRepository.deleteById(id);
            } else {
                if (FORCE.orElse(false)) {
                    studentRepository.deleteById(id);
                } else {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This student has enrollment history. It is not recommended to delete.");
                }
            }
        }
    }

}
