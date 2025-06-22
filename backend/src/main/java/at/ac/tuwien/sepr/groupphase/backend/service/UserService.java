package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.message.SimpleMessageDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user.LockedUserDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user.UserLoginDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user.UserRegisterDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user.UserUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import org.springframework.data.domain.Page;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService extends UserDetailsService {

    /**
     * Find a user in the context of Spring Security based on the email address.
     * <br>
     * For more information have a look at this tutorial:
     * https://www.baeldung.com/spring-security-authentication-with-a-database
     *
     * @param email the email address
     * @return a Spring Security user
     * @throws UsernameNotFoundException is thrown if the specified user does not
     *                                   exists
     */
    @Override
    UserDetails loadUserByUsername(String email) throws UsernameNotFoundException;

    /**
     * Find an application user based on the email address.
     *
     * @param email the email address
     * @return a application user
     */
    ApplicationUser findApplicationUserByEmail(String email);

    /**
     * Log in a user.
     *
     * @param userLoginDto login credentials
     * @return the JWT, if successful
     * @throws BadCredentialsException if credentials are bad
     */
    String login(UserLoginDto userLoginDto);


    /**
     * Find an application user based on the id.
     *
     * @param id the id of the user
     * @return an application user
     */
    ApplicationUser findUserById(Long id);

    /**
     * Register a new user.
     *
     * @param userRegisterDto registration information
     */
    void register(UserRegisterDto userRegisterDto) throws ValidationException;

    /**
     * Returns all locked users, only administrators
     * should be able to access this method.
     *
     * @return all blocked users
     */
    List<LockedUserDto> getLockedUsers();

    /**
     * Returns all users, only administrators
     * should be able to access this method.
     *
     * @return a page of all users
     */
    Page<LockedUserDto> getAllUsersPaginated(Long currentUserId, Pageable pageable);

    /**
     * Unlocks the user account with the given ID by setting its 'locked' status to false.
     *
     * @param id the ID of the user to unlock
     */
    void unlockUser(Long id);

    /**
     * Blocks the user account with the given ID by setting its 'locked' status to true.
     *
     * @param id the ID of the user to block
     */
    void blockUser(Long id);

    /**
     * Sends a password reset to the user-email.
     *
     * @param id the ID of the user to block
     */
    void resetPassword(Long id);

    /**
     * Delete user.
     *
     * @param id of user to delete
     */
    void delete(Long id);

    /**
     * Update user.
     *
     * @param id           of user
     * @param userToUpdate updated user details
     */
    void update(Long id, UserUpdateDto userToUpdate) throws ValidationException;

    /**
     * Get all messages that the user has not seen yet.
     *
     * @param userId the id of the user
     * @return list of unseen messages
     */
    List<SimpleMessageDto> getUnseenMessages(Long userId);

    /**
     * Get all messages that the user has not seen yet, paginated.
     *
     * @param userId   the id of the user
     * @param pageable the pagination information
     * @return paginated list of unseen messages
     */
    Page<SimpleMessageDto> getUnseenMessagesPaginated(Long userId, Pageable pageable);

    /**
     * Marks the given message as seen for the user.
     *
     * @param userId    the id of the user
     * @param messageId the id of the message to mark as seen
     */
    void markMessageAsSeen(Long userId, Long messageId);
}
