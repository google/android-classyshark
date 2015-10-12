
public class FancyLibrary {
    private int number;
    
    public void changeNumber() {
        if (number == 0) {
            System.out.println("thread " + Thread.currentThread().getName() +
                    " ==> wrote value");
            number = -1;
        } else {
            System.out.println("thread " + Thread.currentThread().getName() +
                    " ==> read value");
        }
    }
}
