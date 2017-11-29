package com.basheerbecerra.melanogasteratles.controllers;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
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
			@RequestParam(value = "seconds", defaultValue = "5") Integer seconds)
			throws IOException, InterruptedException {
		Runtime run = Runtime.getRuntime();
		// Capture Video
		Process makeCaptureDir = run.exec("mkdir ./src/main/resources/capture");
		System.out.println(makeCaptureDir.waitFor());

		Process takeVideo = run.exec("raspivid -o ./src/main/resources/capture/capture.h264 -t " + seconds*1000);
		System.out.println(takeVideo.waitFor());
		// Analyze Video

		// Edit Video (Overlay)

		// Add edited video to static

		// Retrieve Stats

		// Organize Results
		Process convertVideo = run.exec("MP4Box -add ./src/main/resources/capture/capture.h264 ./src/main/resources/capture/capture.mp4");
		System.out.println(convertVideo.waitFor());
		Process moveVideoToStatic = run.exec("mv ./src/main/resources/capture/capture.mp4 ./src/main/resources/static");
		System.out.println(moveVideoToStatic.waitFor());
		Thread.sleep(6000);
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
	public void getFile(HttpServletResponse response) {
		try {
			response.setContentType("video/mp4");
			// get your file as InputStream
			FileSystemResource resource = new FileSystemResource("src/main/resources/static/capture.mp4");
			InputStream is = resource.getInputStream();
			// copy it to response's OutputStream
			IOUtils.copy(is, response.getOutputStream());
			response.flushBuffer();
		} catch (IOException ex) {
			throw new RuntimeException("IOError writing file to output stream");
		}

	}
}
