package si.banka.korisnicki_servis.service.implementation;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.MessagingException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import si.banka.korisnicki_servis.controller.response_forms.CreateUserForm;
import si.banka.korisnicki_servis.mail.PasswordResetToken;
import si.banka.korisnicki_servis.model.Permissions;
import si.banka.korisnicki_servis.model.Role;
import si.banka.korisnicki_servis.model.User;
import si.banka.korisnicki_servis.repository.PasswordTokenRepository;
import si.banka.korisnicki_servis.repository.RoleRepository;
import si.banka.korisnicki_servis.repository.UserRepository;
import si.banka.korisnicki_servis.service.UserService;
import javax.jms.Queue;

import javax.transaction.Transactional;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserServiceImplementation implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordTokenRepository passwordTokenRepository;
    @Autowired
    JmsTemplate jmsTemplate;
    @Autowired
    Queue mailQueue;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);
        if(user == null || !(user.isAktivan())){
            log.error("User {} not found in database", username);
            throw new UsernameNotFoundException("User not found in database");
        }
        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        user.getRole().getPermissions().forEach(permission -> {
            authorities.add(new SimpleGrantedAuthority(permission));
        });

        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), authorities);
    }

    @Override
    public User getUser(String username) {
        log.info("Showing user {}", username);
        return userRepository.findByUsername(username);
    }

    @Override
    public Optional<User> getUserById(long id) {
        return userRepository.findById(id);
    }

    @Override
    public List<User> getUsers() {
        log.info("Showing list of users");
        return userRepository.findAll();
    }

    @Override
    public Role getRole(String role_name) {
        return roleRepository.findByName(role_name);
    }

    @Override
    public void deleteUser(User user) {
        if(user.getRole().getName().equalsIgnoreCase("ROLE_GL_ADMIN"))
            return;

        log.info("Setting user inactive {} in database", user.getUsername());
        user.setAktivan(false);
    }

    @Override
    public User createUser(CreateUserForm createUserForm) {
        String username = createUserForm.getIme().toLowerCase()+ "." + createUserForm.getPrezime().toLowerCase();
        if(this.getUser(username) instanceof User){
            username = username + (int)(Math.random() * (100)) + 1;
        }
        String password = createUserForm.getIme() + "Test123";
        User user = new User(username, createUserForm.getIme(),
                            createUserForm.getPrezime(), createUserForm.getEmail(),
                            createUserForm.getJmbg(), createUserForm.getBr_telefon(),
                            password, true, this.getRole(createUserForm.getPozicija()));
        //Checking password
        String regex = "^(?=.*[A-Z])(?=.*[0-9]).{8,}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(password);
        if(!matcher.matches()) throw new BadCredentialsException("Password: must have 8 characters,one uppercase and one digit minimum");

        log.info("Saving new user {} to the database", user.getUsername());
        String hash_pw = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
        user.setPassword(hash_pw);
        return userRepository.save(user);
    }

    @Override
    public void createUserAdmin(User user){
        log.info("Saving admin to the database");
        String hash_pw = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
        user.setPassword(hash_pw);
        user.setAktivan(true);
        userRepository.save(user);
    }

    @Override
    public User editUser(User user, String token) {
        //Cupamo username i permisije iz tokena
        Algorithm algorithm = Algorithm.HMAC256("secret".getBytes());
        JWTVerifier verifier = JWT.require(algorithm).build();
        DecodedJWT decodedJWT = verifier.verify(token);

        String usernameFromJWT = decodedJWT.getSubject();
        String[] permissionsFromJWT = decodedJWT.getClaim("permissions").asArray(String.class);
        //Ako si to ti, edituj se || Ako nisi mozda je admin || u suprotnom neko hoce da edituje drugog a nije admin
        if(user.getUsername().equalsIgnoreCase(usernameFromJWT) && hasEditPermission(permissionsFromJWT, Permissions.MY_EDIT)) {
            //edit logika set na usera?
        }else if(hasEditPermission(permissionsFromJWT, Permissions.EDIT_USER)){
            //edit logika
        }

        return user;
    }

    @Override
    public Role saveRole(Role role) {
        log.info("Saving new role {} to the database", role.getName());
        return roleRepository.save(role);
    }

    @Override
    public void setRoleToUser(String username, String role_name) {
        log.info("Adding role {} to user {} to the database", role_name, username);
        User user = userRepository.findByUsername(username);
        Role role = roleRepository.findByName(role_name);
        user.setRole(role);
    }

    public boolean hasEditPermission(String[] permissions,Permissions permission){
        if(Arrays.stream(permissions).anyMatch(pm -> pm.equalsIgnoreCase(String.valueOf(permission)))){
            return true;
        }
        return false;
    }

    public void createPasswordResetTokenForUser(User user, String token) {
        PasswordResetToken myToken = new PasswordResetToken(token, user);
        passwordTokenRepository.save(myToken);
    }

    @Override
    public User getUserByEmail(String email){
        for(User user : this.getUsers()){
            if(user.getEmail().equalsIgnoreCase(email)){
                return user;
            }
        }
        return null;
    }

    @Override
    public boolean resetPassword(String email){
        User user = this.getUserByEmail(email);
        if(user == null){
            return false;
        }

        String token = UUID.randomUUID().toString();
        this.createPasswordResetTokenForUser(user, token);
        this.sendMail(email, token);

        return true;
    }

    public void sendMail(String email, String token) throws MessagingException {
        String to = email;
        String url = "localhost:8080/user/change-password/" + token;
        String subject = "Password reset";
        String content = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<body>\n" +
                "\n" +
                "<h1>Link ka resetovanju passworda</h1>\n" +
                "\n" +
                "<p>"+ url +"</p>\n" +
                "\n" +
                "</body>\n" +
                "</html>";
        jmsTemplate.convertAndSend(mailQueue , to + "###" + subject + "###" + content);
    }

    @Override
    public boolean setNewPassword(String password, String token) {
        PasswordResetToken prt = this.passwordTokenRepository.findByToken(token);
        if(prt == null){
            return false;
        }

        //Checking password
        String regex = "^(?=.*[A-Z])(?=.*[0-9]).{8,}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(password);
        if(!matcher.matches()) throw new BadCredentialsException("Password: must have 8 characters,one uppercase and one digit minimum");

        User user = prt.getUser();
        user.setPassword(password);
        this.userRepository.save(user);
        return true;
    }
}
