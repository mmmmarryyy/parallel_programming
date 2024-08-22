import org.javatuples.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;

public class SetImpl<T extends Comparable<T>> implements Set<T> {

    private final Node<T> head;

    public SetImpl() {
        head = new Node<>(null);
    }

    private @NotNull Pair<Node<T>, Node<T>> searchForValueWithMark(@NotNull T value, boolean marked) {
        Node<T> previousNode = head;
        Node<T> nextNode = (Node<T>) head.nextNode.getReference();

        while (nextNode != null) {
            if (nextNode.nextNode.isMarked() == marked && value.equals(nextNode.data)) {
                break;
            }

            previousNode = nextNode;
            nextNode = (Node<T>) nextNode.nextNode.getReference();
        }

        return new Pair<>(previousNode, nextNode);
    }

    private @NotNull ArrayList<Node<T>> snapshot() {
        ArrayList<Node<T>> arrayList = new ArrayList<>();
        
        for (Node<T> node = head; node != null; node = (Node<T>) node.nextNode.getReference()) {
            if (node != head && !node.nextNode.isMarked()) {
                arrayList.add(node);
            }
        }
        
        return arrayList;
    }

    @Override
    public boolean add(T value) {
        while (true) {
            Pair<Node<T>, Node<T>> valuePosition = searchForValueWithMark(value, false);

            if (valuePosition.getValue1() != null) {
                return false;
            }

            Node<T> newNode = new Node<>(value);

            if (valuePosition.getValue0().nextNode.compareAndSet(null, newNode, false, false)) {
                return true;
            }
        }
    }

    @Override
    public boolean remove(T value) {
        while (true) {
            Pair<Node<T>, Node<T>> valuePosition = searchForValueWithMark(value, false);
            Node<T> nextNode = valuePosition.getValue1();

            if (nextNode == null) {
                return false; //because nextNode's value should be equal to us
            }

            Node<T> nextNextNode = (Node<T>) nextNode.nextNode.getReference();

            if (nextNode.nextNode.compareAndSet(nextNextNode, nextNextNode, false, true)) {
                while (true) {
                    valuePosition = searchForValueWithMark(value, true);
                    nextNode = valuePosition.getValue1();

                    if (valuePosition.getValue0().nextNode.compareAndSet(nextNode, nextNode.nextNode.getReference(), false, false)) {
                        return true;
                    }
                }
            }
        }
    }

    @Override
    public boolean contains(T value) {
        return searchForValueWithMark(value, false).getValue1() != null;
    }

    @Override
    public boolean isEmpty() {
        for (Node<T> node = head; node != null; node = (Node<T>) node.nextNode.getReference()) {
            if (node != head && !node.nextNode.isMarked()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Iterator<T> iterator() {
        while (true) {
            ArrayList<Node<T>> firstSnapshot = snapshot();
            ArrayList<Node<T>> secondSnapshot = snapshot();

            if (firstSnapshot.size() == secondSnapshot.size() && firstSnapshot.containsAll(secondSnapshot)) {
                return firstSnapshot.stream().map(node -> node.data).iterator();
            }
        }
    }
}
