package com.xuan.tft.tft_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.xuan.tft.tft_backend.entity.Champion;
import com.xuan.tft.tft_backend.entity.Trait;
import com.xuan.tft.tft_backend.repository.ChampionRepository;
import com.xuan.tft.tft_backend.repository.TraitRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class TftBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(TftBackendApplication.class, args);
	}

	@Bean
	public CommandLineRunner dataLoader(ChampionRepository championRepository, TraitRepository traitRepository) {
		return args -> {
			// 初始化数据（模拟用
			if (championRepository.count() == 0 && traitRepository.count() == 0) {
				Trait mage = new Trait("Mage", "Increases ability power.");
				Trait warden = new Trait("Warden", "Increases armor and durability.");
				Trait swordsman = new Trait("Swordsman", "Basic attacks can strike additional times.");

				traitRepository.save(mage);
				traitRepository.save(warden);
				traitRepository.save(swordsman);

				// 创建棋子+关联羁绊
				Champion ahri = new Champion("Ahri", 3);
				ahri.getTraits().add(mage);

				Champion garen = new Champion("Garen", 1);
				garen.getTraits().add(warden);

				Champion yasuo = new Champion("Yasuo", 4);
				yasuo.getTraits().add(swordsman);

				Champion lux = new Champion("Lux", 4);
				lux.getTraits().add(mage);

				Champion aatrox = new Champion("Aatrox", 5);
				aatrox.getTraits().add(swordsman);
				aatrox.getTraits().add(warden);

				championRepository.save(ahri);
				championRepository.save(garen);
				championRepository.save(yasuo);
				championRepository.save(lux);
				championRepository.save(aatrox);
			}
		};
	}
}
