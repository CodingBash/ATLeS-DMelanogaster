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
			@RequestParam(value = "length", defaultValue = "5") Integer seconds)
			throws IOException, InterruptedException {
		Runtime run = Runtime.getRuntime();

		/*
		 * Make directory to contain captured video from pi
		 */
		Process makeCaptureDir = run.exec("mkdir ./src/main/resources/capture");
		System.out.println("Make Capture Directory Result: " + makeCaptureDir.waitFor());

		/*
		 * Take the video (only works on pi environment)
		 */
		Process takeVideo = run.exec("raspivid -o ./src/main/resources/capture/capture.h264 -t " + seconds * 1000);
		System.out.println("Take Video Result: " + takeVideo.waitFor());

		/*
		 * Convert the video from .h264 to .mp4 (only works on environment w/ MP4Box)
		 */
		Process convertVideo = run
				.exec("MP4Box -add ./src/main/resources/capture/capture.h264 ./src/main/resources/capture/capture.mp4");
		System.out.println("Convert Capture from .h264 to .mp4 Result: " + convertVideo.waitFor());

		/*
		 * Remove any previous capture from static folder
		 */
		Process removePreviousVideo = run.exec("rm -f ./src/main/resources/static/capture.mp4");
		System.out.println("Remove Previous Video Result: " + removePreviousVideo.waitFor());

		/*
		 * Rename file
		 */
		Process renameToUpload= run.exec("mv ./src/main/resources/capture/capture.mp4 ./src/main/resources/capture/upload.mp4");
		System.out.println("Rename Capture to Upload Result: " + renameToUpload.waitFor());
		
		/*
		 * Delete everything from static directory
		 */
		Process deleteAll = run.exec("rm -rf src/main/resources/static/");
		System.out.println("Delete static directory result: " + deleteAll.waitFor());
		
		/*
		 * Recreate static directory
		 * 
		 */
		Process createStaticDir = run.exec("mkdir src/main/resources/static/");
		System.out.println("Recreate static directory result: " + createStaticDir.waitFor());
		
		/*
		 * Move the capture result to the static folder
		 */
		Process moveVideoToStatic = run.exec("mv ./src/main/resources/capture/upload.mp4 ./src/main/resources/static");
		System.out.println("Move capture to static directory result: " + moveVideoToStatic.waitFor());

		//TODO: Duplicate code, move to method
		/*
		 * Run the OpenCV analysis on the uploaded file
		 */
		Process runAnalysis = run.exec("python analyze.py");
		/*
		 * Stream feed to prevent deadlock
		 */
		InputStream stderr1 = runAnalysis.getInputStream();
		InputStreamReader isr1 = new InputStreamReader(stderr1);
		BufferedReader br1 = new BufferedReader(isr1);
		String line1 = null;
		while ((line1 = br1.readLine()) != null)
			System.out.println(line1);
		
		int analysisExitCode = runAnalysis.waitFor();
		
		System.out.println("OpenCV Analysis Result: " + analysisExitCode);
		/*
		 * If analysis was successful
		 */
		if (analysisExitCode == 0) {
			/*
			 * Convert the analysis result video from .avi to .mp4 (only works with environment with ffmpeg installed)
			 */
			Process convert = run.exec(
					"ffmpeg -i src/main/resources/static/capture.avi -c:v libx264 -crf 19 -preset slow -c:a libfdk_aac -b:a 192k -ac 2 src/main/resources/static/capture.mp4");
			
			/*
			 * Stream feed to prevent deadlock
			 */
			InputStream stderr = convert.getErrorStream();
			InputStreamReader isr = new InputStreamReader(stderr);
			BufferedReader br = new BufferedReader(isr);
			String line = null;
			while ((line = br.readLine()) != null)
				System.out.println(line);
			int convertExitCode = convert.waitFor();
			System.out.println("Convert Analysis Video from .avi to .mp4 Result: " + convertExitCode);
			/*
			 * If conversion was successful
			 */
			if (convertExitCode == 0) {
				/*
				 * Redirect to result page
				 */
				return "redirect:/results";
			}
		}
		
		/*
		 * If an errors, redirect back to home page 
		 * TODO: Add flash attribute with error
		 */
		return "redirect:/";
		
	}

	@RequestMapping(method = RequestMethod.GET, value = "/results")
	public ModelAndView results(@ModelAttribute("test") String test) {
		ModelAndView mav = new ModelAndView("results");
		return mav;
	}

	@PostMapping("/upload")
	public String handleFileUpload(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes)
			throws InterruptedException, IOException {
		Runtime run = Runtime.getRuntime();
		/*
		 * Delete everything from static directory
		 */
		Process deleteAll = run.exec("rm -rf src/main/resources/static/");
		System.out.println("Delete All in Static Result: " + deleteAll.waitFor());
		/*
		 * Recreate static directory
		 * 
		 */
		Process createStaticDir = run.exec("mkdir src/main/resources/static/");
		System.out.println("Create static directory: " +createStaticDir.waitFor());
		/*
		 * Store the uploaded file
		 */
		service.store(file);
		System.out.println("Stored Filed");
		/*
		 * Run the OpenCV analysis on the uploaded file
		 */
		Process runAnalysis = run.exec("python analyze.py");
		InputStream stderr1 = runAnalysis.getInputStream();
		InputStreamReader isr1 = new InputStreamReader(stderr1);
		BufferedReader br1 = new BufferedReader(isr1);
		String line1 = null;
		while ((line1 = br1.readLine()) != null)
			System.out.println(line1);
		
		int analysisExitCode = runAnalysis.waitFor();
		
		System.out.println("OpenCV Analysis Result: " + analysisExitCode);
		/*
		 * If analysis was successful
		 */
		if (analysisExitCode == 0) {
			/*
			 * Convert the analysis result video from .avi to .mp4 (only works with environment with ffmpeg installed)
			 */
			Process convert = run.exec(
					"ffmpeg -i src/main/resources/static/capture.avi -c:v libx264 -crf 19 -preset slow -c:a libfdk_aac -b:a 192k -ac 2 src/main/resources/static/capture.mp4");
			
			/*
			 * Stream feed to prevent deadlock
			 */
			InputStream stderr = convert.getErrorStream();
			InputStreamReader isr = new InputStreamReader(stderr);
			BufferedReader br = new BufferedReader(isr);
			String line = null;
			while ((line = br.readLine()) != null)
				System.out.println(line);
			int convertExitCode = convert.waitFor();
			System.out.println("Convert Analysis Video from .avi to .mp4 Result: " + convertExitCode);
			/*
			 * If conversion was successful
			 */
			if (convertExitCode == 0) {
				/*
				 * Redirect to result page
				 */
				return "redirect:/results";
			}
		}
		
		/*
		 * If an errors, redirect back to home page 
		 * TODO: Add flash attribute with error
		 */
		return "redirect:/";

	}

	@RequestMapping(value = "/video", method = RequestMethod.GET)
	public void getFile(HttpServletResponse response) throws IOException {
		try {
			response.setContentType("video/mp4");
			FileSystemResource resource = new FileSystemResource("src/main/resources/static/capture.mp4");
			InputStream is = resource.getInputStream();
			IOUtils.copy(is, response.getOutputStream());
			response.flushBuffer();
		} catch (IOException ex) {
			throw new RuntimeException("IOError writing file to output stream");
		}

	}
}
