package com.basheerbecerra.melanogasteratles.controllers;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class MainController {

	@RequestMapping(method = RequestMethod.GET, value = "/")
	public String home() {
		return "home";
	}

	@RequestMapping(method = RequestMethod.POST, value = "/record")
	public String record(RedirectAttributes redirectAttributes,
			@RequestParam(value = "seconds", defaultValue = "5") Integer seconds) throws IOException, InterruptedException {
		Runtime run = Runtime.getRuntime();
		// Capture Video
		run.exec("mkdir ./src/main/resources/capture");
		run.exec("raspivid -o ./src/main/resources/capture/capture.h264 -t " + seconds);
		Thread.sleep(seconds * 1000 + 1000);
		// Analyze Video
		
		
		// Edit Video (Overlay)
		
		
		// Add edited video to static
		
		
		
		// Retrieve Stats
		
		
		// Organize Results
		
		run.exec("mv ./src/main/resources/capture/capture.h264 ./src/main/resources/static");
		run.exec("MP4Box -add 
		
		System.out.println("Here");
		redirectAttributes.addFlashAttribute("test", "test");
		return "redirect:/results";
	}

	@RequestMapping(method = RequestMethod.GET, value = "/results")
	public ModelAndView results(@ModelAttribute("test") String test) {
		ModelAndView mav = new ModelAndView("results");
		mav.addObject("test", test);
		return mav;
	}

	@RequestMapping(value = "/video", method = RequestMethod.GET)
	@ResponseBody
	public FileSystemResource getPreview3(@PathVariable("id") String id, HttpServletResponse response) {
		String path = getClass().getResource("classpath:/capture/movie.h264").toString();
		return new FileSystemResource(path);
	}
}
