package com.community.dogcat.controller.error;

import javax.servlet.http.HttpServletRequest;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.community.dogcat.controller.BaseController;
import com.community.dogcat.jwt.JWTUtil;
import com.community.dogcat.service.user.UserService;

@Controller
public class CustomErrorController extends BaseController implements ErrorController  {

	public CustomErrorController(JWTUtil jwtUtil,
		UserService userService) {
		super(jwtUtil, userService);
	}

	private static final String PATH = "/error";


	@RequestMapping(value = PATH)
	public String error(HttpServletRequest request) {

		Integer statusCode = (Integer)request.getAttribute("javax.servlet.error.status_code");

		if (statusCode != null) {

			return "error/accessDenied";

		}

		return "error/accessDenied";

	}

}
