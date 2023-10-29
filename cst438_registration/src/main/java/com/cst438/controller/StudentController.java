package com.cst438.controller;


import com.cst438.domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.websocket.server.PathParam;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
public class StudentController {
    @Autowired
    StudentRepository studentRepository;

    @Autowired
    EnrollmentRepository enrollmentRepository;

    @GetMapping("/student")
    @CrossOrigin
    public List<StudentDTO> listStudents() {
        List<StudentDTO> allStudents = new ArrayList<>();
        for(Student s : studentRepository.findAll()) {
            StudentDTO student = new StudentDTO(s.getStudent_id(), s.getName(), s.getEmail(), s.getStatusCode(), s.getStatus());
            allStudents.add(student);
        }
        return allStudents;
    }

    @PostMapping("/student")
    @CrossOrigin
    public int addStudent(Principal principal, @RequestBody StudentDTO s) {
        String email = principal.getName();
        Student student = studentRepository.findByEmail(s.email());
        // Can successfully add a student record when the email is distinct
        if (student == null) {
            student = new Student();
            student.setName(s.name());
            student.setEmail(s.email());
            student.setStatusCode(s.statusCode());
            student.setStatus(s.status());
            studentRepository.save(student);
            return student.getStudent_id();
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "A student with that email already exists.");
        }
    }

    @PutMapping("/student/{id}")
    @CrossOrigin
    public Student update(Principal principal, @PathVariable("id") Integer id, @RequestBody StudentDTO us) {
        String email = principal.getName();
        Student student = studentRepository.findById(id).orElse(null);
        if(student != null) {
            Student existingEmail = studentRepository.findByEmail(us.email());
            if(existingEmail == null || existingEmail.getStudent_id() == id) {
                student.setName(us.name());
                student.setEmail(us.email());
                student.setStatusCode(us.statusCode());
                student.setStatus(us.status());
                return studentRepository.save(student);
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "There is already a student with email: " + us.email());
            }
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "There is no student with id " + id);
        }

    }


    @DeleteMapping("/student/{id}")
    @CrossOrigin
    public void delete(
            Principal principal,
            @PathVariable("id") Integer id,
            @RequestParam("FORCE")Optional<Boolean> FORCE) {
        String email = principal.getName();
        Enrollment[] studentInEnrollment = enrollmentRepository.findStudentInEnrollment(id);
        Student student =  studentRepository.findById(id).orElse(null);

        /*
        * Deletes a student unless a student has an enrollment
        * When there is an enrollment deletion can still occur when the FORCE paramter is entered
        * */
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
