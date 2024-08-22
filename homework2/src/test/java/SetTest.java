import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;

import static org.junit.Assert.*;

public class SetTest {

    @Test
    public void add() {
        Set<Integer> set = new SetImpl<>();

        for (int i = 0; i < 50; i++) {
            assertTrue(set.add(i));
        }

        for (int i = 0; i < 50; i++) {
            assertFalse(set.add(i));
        }
    }

    @Test
    public void remove() {
        Set<Integer> set = new SetImpl<>();

        for (int i = 0; i < 50; i++) {
            assertFalse(set.remove(i));
        }

        for (int i = 0; i < 50; i++) {
            assertTrue(set.add(i));
        }

        for (int i = 0; i < 50; i++) {
            assertTrue(set.remove(i));
        }

        for (int i = 0; i < 50; i++) {
            assertFalse(set.remove(i));
        }
    }

    @Test
    public void contains() {
        Set<Integer> set = new SetImpl<>();

        for (int i = 0; i < 100; i++) {
            assertTrue(set.add(i));
        }

        for (int i = 0; i < 100; i++) {
            assertTrue(set.contains(i));
        }

        for (int i = 0; i < 50; i++) {
            assertTrue(set.remove(i));
        }

        for (int i = 0; i < 50; i++) {
            assertFalse(set.contains(i));
        }

        for (int i = 50; i < 100; i++) {
            assertTrue(set.contains(i));
        }
    }

    @Test
    public void isEmpty() {
        Set<Integer> set = new SetImpl<>();

        assertTrue(set.isEmpty());

        for (int i = 0; i < 100; i++) {
            assertTrue(set.add(i));
        }

        assertFalse(set.isEmpty());

        for (int i = 0; i < 50; i++) {
            assertTrue(set.remove(i));
        }

        assertFalse(set.isEmpty());

        for (int i = 50; i < 100; i++) {
            assertTrue(set.remove(i));
        }

        assertTrue(set.isEmpty());
    }

    @Test
    public void iterator() {
        ArrayList<Integer> input = new ArrayList<>();
        Set<Integer> set = new SetImpl<>();

        for (int i = 0; i < 100; i++) {
            assertTrue(input.add(i));
            assertTrue(set.add(i));
        }

        Iterator<Integer> iterator = set.iterator();
        ArrayList<Integer> output = new ArrayList<>();

        while (iterator.hasNext()) {
            output.add(iterator.next());
        }

        output.sort(Integer::compareTo);
        assertArrayEquals(input.toArray(), output.toArray());
    }

    @Test
    public void nodeEquals() {
        ArrayList<Integer> input = new ArrayList<>();
        Node node1 = new Node(10);
        Node node2 = new Node(10);
        System.out.println("node1 version = " + node1.version);
        System.out.println("node2 version = " + node2.version);
        assertFalse(node1.version == node2.version);
        assertFalse(node1 == node2);
    }

    @Test
    public void emptySet() {
        Set<Integer> set = new SetImpl<>();

        for (int i = 0; i < 100; i++) {
            assertTrue(set.add(i));
            assertTrue(set.remove(i));
            assertTrue(set.isEmpty());
            assertFalse(set.contains(i));
            assertFalse(set.remove(i));
        }

        Iterator<Integer> iterator = set.iterator();
        assertTrue(!iterator.hasNext());
    }
}