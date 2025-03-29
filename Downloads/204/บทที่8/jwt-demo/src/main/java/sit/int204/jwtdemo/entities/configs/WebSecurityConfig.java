package sit.int204.jwtdemo.entities.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

//ซึ่งใช้ในการตั้งค่าการควบคุมการเข้าถึง (access control) สำหรับแอปพลิเคชันที่ใช้ Spring Boot โดยตั้งค่าต่างๆ ผ่าน HttpSecurity ใน Spring Security
@EnableWebSecurity //ใช้ในการเปิดใช้งาน Spring Security ในแอปพลิเคชัน ทำให้สามารถตั้งค่าความปลอดภัยของเว็บแอปพลิเคชันได้
@Configuration //ที่ใช้ในการกำหนดการตั้งค่าต่างๆ ของ Spring
public class WebSecurityConfig {

    //การใช้ HttpSecurity ช่วยให้สามารถกำหนดกฎการอนุญาต (authorization) การป้องกัน CSRF, และการจัดการ session ได้
    @Bean //ที่ใช้กำหนดค่าการตั้งค่า HttpSecurity ซึ่งเป็นตัวควบคุมการตั้งค่าความปลอดภัยของแอปพลิเคชัน
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        //ปิดการตั้งค่า frame options เพื่อไม่ให้ error
        http.headers(httpSecurityHeadersConfigurer ->
                httpSecurityHeadersConfigurer.frameOptions(frameOptionsConfig ->
                        frameOptionsConfig.disable()));

        //ปิดการตรวจสอบ Cross-Site Request Forgery (CSRF) ซึ่งปกติแล้ว Spring Security จะเปิดใช้งาน CSRF protection
        //แต่ในกรณีนี้ปิดการป้องกัน CSRF เนื่องจากอาจใช้ API แบบ stateless หรือการตั้งค่าอื่นๆ ที่ไม่ต้องการ CSRF protection
        http.csrf(crsf -> crsf.disable())

                //กำหนดให้ทุกคำขอ (request) ในแอปพลิเคชันสามารถเข้าถึงได้โดยไม่ต้องมีการตรวจสอบสิทธิ์ (ทุกหน้าสามารถเข้าถึงได้ไม่จำกัด)
                .authorizeHttpRequests((requests) -> requests
                        .anyRequest().permitAll()
                )

                //กำหนด session ให้เป็น stateless คือแอปพลิเคชันจะไม่เก็บสถานะของ session ระหว่างคำขอ (request) ทุกคำขอจะไม่ถูกผูกกับ session ใดๆ
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        //สร้างและคืนค่าการตั้งค่าความปลอดภัยที่ถูกกำหนดใน http เพื่อให้ Spring Security ใช้ในการรักษาความปลอดภัยของแอปพลิเคชัน
        return http.build();
    }
}
