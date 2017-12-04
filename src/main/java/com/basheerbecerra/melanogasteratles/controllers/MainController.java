package com.basheerbecerra.melanogasteratles.controllers;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.basheerbecerra.melanogasteratles.services.FileSystemStorageService;

@Controller
public class MainController {

	@Autowired
	FileSystemStorageService service;

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

		Process takeVideo = run.exec("raspivid -o ./src/main/resources/capture/capture.h264 -t " + seconds * 1000);
		System.out.println(takeVideo.waitFor());

		Process convertVideo = run
				.exec("MP4Box -add ./src/main/resources/capture/capture.h264 ./src/main/resources/capture/capture.mp4");
		System.out.println(convertVideo.waitFor());

		Process removePreviousVideo = run.exec("rm -f ./src/main/resources/static/capture.mp4");
		removePreviousVideo.waitFor();

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

	@PostMapping("/upload")
	public String handleFileUpload(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes)
			throws InterruptedException, IOException {
		Runtime run = Runtime.getRuntime();

		service.store(file);

		Process runAnalysis = run.exec("python analyze.py");
		System.out.println(runAnalysis.isAlive());
		System.out.println(runAnalysis.waitFor());
		
		Process convert = run.exec("ffmpeg -i src/main/resources/static/capture.avi -c:v libx264 -crf 19 -preset slow -c:a libfdk_aac -b:a 192k -ac 2 src/main/resources/static/capture.mp4");
		InputStream stderr = convert.getErrorStream();
        InputStreamReader isr = new InputStreamReader(stderr);
        BufferedReader br = new BufferedReader(isr);
        String line = null;
        System.out.println("<ERROR>");
        while ( (line = br.readLine()) != null)
            System.out.println(line);
        System.out.println("</ERROR>");
		System.out.println(convert.waitFor());
		while (true) {
			File f = new File("src/main/resources/static/capture.mp4");
			if (f.exists() && !f.isDirectory()) {
				break;
			}
		}

		System.out.println("Complete");
		return "redirect:/results";
	}

	@RequestMapping(value = "/video", method = RequestMethod.GET)
	public void getFile(HttpServletResponse response) throws IOException {
		try {
			response.setContentType("video/mp4");
			// get your file as InputStream
			FileSystemResource resource = new FileSystemResource("src/main/resources/static/capture.mp4");
			InputStream is = resource.getInputStream();
			// copy it to response's OutputStream
			IOUtils.copy(is, response.getOutputStream());
			response.flushBuffer();
		} catch (IOException ex) {
			response.flushBuffer();
			// throw new RuntimeException("IOError writing file to output
			// stream");
		}

	}
}
