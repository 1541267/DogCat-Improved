package com.community.dogcat.service.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.community.dogcat.repository.user.VetRepository;

@Service
public class VetService {

	@Autowired
	private VetRepository vetRepository;

	public boolean verifyVet(String vetName, Long vetLicense) {
		// 데이터베이스에서 수의사 이름과 번호를 확인
		return vetRepository.existsByVetNameAndVetLicense(vetName, vetLicense);
	}
}
