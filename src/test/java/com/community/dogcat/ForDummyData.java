package com.community.dogcat;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.community.dogcat.domain.UsersVet;
import com.community.dogcat.repository.user.VetRepository;

@SpringBootTest
class ForDummyData {

	@Autowired
	private VetRepository vetRepository;

	@Test
	public void testInsertVets() {

		UsersVet vet1 = new UsersVet("김윤섭", 11111L, false);
		UsersVet vet2 = new UsersVet("박민우", 22222L, false);
		UsersVet vet3 = new UsersVet("김주희", 33333L, false);
		UsersVet vet4 = new UsersVet("구현석", 44444L, false);
		UsersVet vet5 = new UsersVet("김슬기", 55555L, false);
		UsersVet vet6 = new UsersVet("황서진", 84063L, false);
		UsersVet vet7 = new UsersVet("배은찬", 17729L, false);
		UsersVet vet8 = new UsersVet("마연우", 41061L, false);
		UsersVet vet9 = new UsersVet("곽두팔", 57257L, false);
		UsersVet vet10 = new UsersVet("고준서", 42784L, false);

		vetRepository.save(vet1);
		vetRepository.save(vet2);
		vetRepository.save(vet3);
		vetRepository.save(vet4);
		vetRepository.save(vet5);
		vetRepository.save(vet6);
		vetRepository.save(vet7);
		vetRepository.save(vet8);
		vetRepository.save(vet9);
		vetRepository.save(vet10);

		List<UsersVet> vets = vetRepository.findAll();
		assertEquals(10, vets.size());

	}
}
