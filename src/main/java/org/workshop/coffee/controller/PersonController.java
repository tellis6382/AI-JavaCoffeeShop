package org.workshop.coffee.controller;

import org.workshop.coffee.domain.Person;
import org.workshop.coffee.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.security.SecureRandom;
import java.util.Base64;

@Controller
@RequestMapping("/persons")
public class PersonController {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final PersonService personService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public PersonController(PersonService personService, PasswordEncoder passwordEncoder) {
        this.personService = personService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public String listPersons(Model model) {
        model.addAttribute("persons", personService.getAllPersons());
        return "person/list";
    }

    @GetMapping("/edit/{id}")
    public String showPersonEdit(@PathVariable Long id, Model model) {
        Person person = personService.findById(id);
        model.addAttribute("person", person);
        return "person/edit";
    }

    @GetMapping("/add")
    public String showPersonAdd(Model model) {
        model.addAttribute("person", new Person());
        return "person/edit";
    }

    @PostMapping({"/edit/{id}", "/add"})
    public String savePerson(@Valid Person person, BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "person/edit";
        }

        boolean isAdd = person.getId() == null;

        if (isAdd) {
            byte[] tempBytes = new byte[24];
            SECURE_RANDOM.nextBytes(tempBytes);
            String tempPassword = Base64.getUrlEncoder().withoutPadding().encodeToString(tempBytes);
            person.setPassword(passwordEncoder.encode(tempPassword));
        }

        personService.savePerson(person);

        if (isAdd) {
            redirectAttributes.addFlashAttribute("message", "Person has been created successfully.");
        } else {
            redirectAttributes.addFlashAttribute("message", "Person has been updated successfully.");
        }

        return "redirect:/persons/edit/" + person.getId();
    }

    @GetMapping("/delete/{id}")
    public String deletePerson(@PathVariable Long id) {
        personService.removePerson(id);
        return "redirect:/persons";
    }

}
