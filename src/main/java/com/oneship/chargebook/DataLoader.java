package com.oneship.chargebook;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.oneship.chargebook.repository.ChargeDataRepository;
import com.oneship.chargebook.repository.UserRepository;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private ChargeDataRepository chargeDataRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        // SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        // // // 사용자 정보를 가져옵니다. 예를 들어, 이메일로 사용자를 찾습니다.
        // User user = userRepository.findByUsername("nestor");

        // List<ChargeData> chargeDataList = Arrays.asList(
        //     new ChargeData(dateFormat.parse("2024-05-01"), 5.03, 1740, 726, 1014, 70, 305, 61, 6150, "삼성", "EV Infra", user),
        //     new ChargeData(dateFormat.parse("2024-05-02"), 35.54, 5290, 0, 5290, 80, 1058, 30, 6368, "BC", "한화모티브", user),
        //     new ChargeData(dateFormat.parse("2024-05-03"), 6.35, 940, 0, 940, 80, 188, 30, 6416, "BC", "한화모티브", user),
        //     new ChargeData(dateFormat.parse("2024-05-10"), 16.6, 2470, 0, 2470, 80, 494, 30, 6428, "BC", "한화모티브", user),
        //     new ChargeData(dateFormat.parse("2024-05-10"), 20.26, 5972, 0, 5972, 70, 1792, 88, 6634, "삼성", "SK일렉링크", user),
        //     new ChargeData(dateFormat.parse("2024-05-11"), 25.69, 8919, 2000, 6919, 80, 1384, 54, 6750, "BC", "환경부", user),
        //     new ChargeData(dateFormat.parse("2024-05-12"), 6.51, 2017, 0, 2017, 70, 605, 93, 6900, "삼성", "E-Pit", user),
        //     new ChargeData(dateFormat.parse("2024-05-12"), 32.02, 4770, 0, 4770, 80, 954, 30, 6977, "BC", "한화모티브", user),
        //     new ChargeData(dateFormat.parse("2024-05-14"), 19.89, 2960, 0, 2960, 80, 592, 30, 7105, "BC", "한화모티브", user),
        //     new ChargeData(dateFormat.parse("2024-05-23"), 28.64, 4260, 0, 4260, 80, 852, 30, 7260, "BC", "한화모티브", user),
        //     new ChargeData(dateFormat.parse("2024-05-25"), 8.14, 1210, 0, 1210, 80, 242, 30, 7384, "BC", "한화모티브", user),
        //     new ChargeData(dateFormat.parse("2024-05-25"), 17.28, 6000, 101, 5899, 70, 1770, 102, 7452, "삼성", "EV Infra", user),
        //     new ChargeData(dateFormat.parse("2024-05-29"), 26.44, 3940, 0, 3940, 80, 788, 30, 7563, "BC", "한화모티브", user),
        //     new ChargeData(dateFormat.parse("2024-05-30"), 4.07, 600, 0, 600, 80, 120, 29, 7585, "BC", "한화모티브", user),
        //     new ChargeData(dateFormat.parse("2024-05-31"), 17.43, 5212, 0, 5212, 80, 1042, 60, 7712, "BC", "etc", user),
        //     new ChargeData(dateFormat.parse("2024-06-02"), 26.04, 3870, 0, 3870, 80, 774, 30, 7836, "BC", "한화모티브", user),
        //     new ChargeData(dateFormat.parse("2024-06-05"), 10.13, 1500, 500, 1000, 80, 200, 20, 7882, "BC", "한화모티브", user),
        //     new ChargeData(dateFormat.parse("2024-06-08"), 15.22, 2260, 0, 2260, 80, 452, 30, 7969, "BC", "한화모티브", user),
        //     new ChargeData(dateFormat.parse("2024-06-12"), 26.67, 3970, 0, 3970, 80, 794, 30, 8112, "BC", "한화모티브", user),
        //     new ChargeData(dateFormat.parse("2024-06-17"), 12.7, 1890, 0, 1890, 80, 378, 30, 8164, "BC", "한화모티브", user),
        //     new ChargeData(dateFormat.parse("2024-06-21"), 11.72, 1740, 0, 1740, 80, 348, 30, 8222, "BC", "한화모티브", user),
        //     new ChargeData(dateFormat.parse("2024-06-21"), 37.19, 10004, 0, 10004, 80, 2001, 54, 8424, "BC", "차지비", user),
        //     new ChargeData(dateFormat.parse("2024-06-23"), 20.75, 6117, 0, 6117, 80, 1223, 59, 8536, "BC", "SK일렉링크", user),
        //     new ChargeData(dateFormat.parse("2024-06-23"), 8.41, 2479, 0, 2479, 80, 496, 59, 8613, "BC", "SK일렉링크", user),
        //     new ChargeData(dateFormat.parse("2024-06-23"), 29.12, 4330, 0, 4330, 80, 866, 30, 8735, "BC", "한화모티브", user),
        //     new ChargeData(dateFormat.parse("2024-06-28"), 17.54, 2610, 0, 2610, 80, 522, 30, 8818, "BC", "한화모티브", user),
        //     new ChargeData(dateFormat.parse("2024-06-29"), 29.84, 4440, 0, 4440, 80, 888, 30, 9009, "BC", "한화모티브", user),
        //     new ChargeData(dateFormat.parse("2024-07-04"), 19.2, 2860, 0, 2860, 80, 572, 30, 9088, "BC", "한화모티브", user),
        //     new ChargeData(dateFormat.parse("2024-07-07"), 28.51, 4240, 0, 4240, 80, 848, 30, 9245, "BC", "한화모티브", user)
        // );

        // chargeDataRepository.saveAll(chargeDataList);
    }
}
