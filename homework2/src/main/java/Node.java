import org.jetbrains.annotations.Nullable;

import java.util.Random;
import java.util.concurrent.atomic.AtomicMarkableReference;

public class Node<T extends Comparable<T>> {
    T data;
    AtomicMarkableReference<Node> nextNode;
    Integer version;

    static final Random random;

    static {
        random = new Random();
    }

    Node(@Nullable T data) {
        this.version = random.nextInt();
        this.nextNode = new AtomicMarkableReference<>(null, false);
        this.data = data;
    }
}
