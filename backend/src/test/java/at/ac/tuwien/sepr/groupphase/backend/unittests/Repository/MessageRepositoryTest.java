package at.ac.tuwien.sepr.groupphase.backend.unittests.Repository;

import at.ac.tuwien.sepr.groupphase.backend.basetest.TestData;
import at.ac.tuwien.sepr.groupphase.backend.config.type.Sex;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.Message;
import at.ac.tuwien.sepr.groupphase.backend.repository.MessageRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@ActiveProfiles("test")
public class MessageRepositoryTest implements TestData {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    private ApplicationUser user1;
    private ApplicationUser user2;
    private Message message1;
    private Message message2;
    private Message message3;

    @BeforeEach
    void setUp() {
        user1 = ApplicationUser.ApplicationUserBuilder.aUser()
            .withFirstName("Test")
            .withLastName("User1")
            .withEmail("user1@test.com")
            .withPassword("password")
            .withDateOfBirth(LocalDate.of(2000, 1, 1))
            .withSex(Sex.MALE)
            .build();
        userRepository.save(user1);

        user2 = ApplicationUser.ApplicationUserBuilder.aUser()
            .withFirstName("Test")
            .withLastName("User2")
            .withEmail("user2@test.com")
            .withPassword("password")
            .withDateOfBirth(LocalDate.of(2000, 1, 1))
            .withSex(Sex.FEMALE)
            .build();
        userRepository.save(user2);

        message1 = Message.MessageBuilder.aMessage()
            .withTitle("Message 1")
            .withSummary("Summary 1")
            .withText("Text 1")
            .withPublishedAt(TEST_NEWS_PUBLISHED_AT.minusDays(1))
            .build();

        message2 = Message.MessageBuilder.aMessage()
            .withTitle("Message 2")
            .withSummary("Summary 2")
            .withText("Text 2")
            .withPublishedAt(TEST_NEWS_PUBLISHED_AT.minusDays(2))
            .build();

        message3 = Message.MessageBuilder.aMessage()
            .withTitle("Message 3")
            .withSummary("Summary 3")
            .withText("Text 3")
            .withPublishedAt(TEST_NEWS_PUBLISHED_AT.minusDays(3))
            .build();

        messageRepository.saveAll(List.of(message1, message2, message3));

        user1.setViewedMessages(new ArrayList<>());
        user1.getViewedMessages().add(message1);
        user1.getViewedMessages().add(message2);

        message1.getViewers().add(user1);
        message2.getViewers().add(user1);

        userRepository.save(user1);
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
        messageRepository.deleteAll();
    }

    @Test
    public void findAllUnseenByUserIdOrderByPublishedAtDesc_shouldReturnCorrectMessages() {
        List<Message> unseenForUser1 = messageRepository.findAllUnseenByUserIdOrderByPublishedAtDesc(user1.getId());
        List<Message> unseenForUser2 = messageRepository.findAllUnseenByUserIdOrderByPublishedAtDesc(user2.getId());

        assertThat(unseenForUser1)
            .hasSize(1)
            .extracting(Message::getTitle)
            .containsExactly("Message 3");

        assertThat(unseenForUser2)
            .hasSize(3)
            .extracting(Message::getTitle)
            .containsExactly("Message 1", "Message 2", "Message 3");
    }
}
