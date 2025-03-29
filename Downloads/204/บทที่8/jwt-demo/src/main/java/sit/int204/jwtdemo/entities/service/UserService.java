package sit.int204.jwtdemo.entities.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import sit.int204.jwtdemo.entities.entities.User;
import sit.int204.jwtdemo.entities.repositories.UserRepository;

import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepo;
    //ถ้าไม่มีข้อมูลซ้ำ จะมีการเข้ารหัสรหัสผ่านของผู้ใช้โดยใช้ Argon2PasswordEncoder และบันทึกข้อมูลผู้ใช้ลงในฐานข้อมูล
    private Argon2PasswordEncoder passwordEncoder =
            new Argon2PasswordEncoder(
                    16, 16,
                    8, 1024*128, 2);

    //ฟังก์ชันนี้ใช้ค้นหาผู้ใช้จากฐานข้อมูลโดยใช้ id
    //ถ้าพบผู้ใช้ที่มี id ตรงกับที่ส่งเข้ามา จะคืนค่าผู้ใช้ (User) ที่ตรงกัน  //ถ้าไม่พบผู้ใช้ จะคืนค่าเป็น null
    public User findUserById(Long id) {
        return userRepo.findById(id).orElse(null);
    }

    //ฟังก์ชันนี้ใช้ตรวจสอบว่า username หรือ email ของผู้ใช้มีอยู่ในฐานข้อมูลแล้วหรือไม่ โดยเรียกใช้ userRepo.existsUserByUsernameOrEmail()
    private void checkDuplication(User user) {
        if (userRepo.existsUserByUsernameOrEmail(
                user.getUsername(), user.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, //ถ้ามีข้อมูลซ้ำ จะโยน"User name or Email already exist !!!
                    "User name or Email already exist !!! ("
                            + user.getUsername() + ", " + user.getEmail() + ')');
        }
    }
        //ฟังก์ชันนี้ใช้ในการสร้างผู้ใช้ใหม่
        public User createUser(User user) {
            checkDuplication(user); //เพื่อเช็คว่ามีผู้ใช้ที่มี username หรือ email ซ้ำกันหรือไม่
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            return userRepo.save(user);
        }

        //ฟังก์ชันนี้ใช้ในการสร้างผู้ใช้หลายคนในครั้งเดียว
        public List<User> createUsers(List<User> users) {
            for (User user : users) {
                checkDuplication(user); //เพื่อเช็คว่ามีผู้ใช้ที่มี username หรือ email ซ้ำกันหรือไม่
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            }
            return userRepo.saveAll(users);
        }
    }
