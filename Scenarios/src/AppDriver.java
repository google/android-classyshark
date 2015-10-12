public class AppDriver {

    public static void main(String[] args) throws InterruptedException {
        final FancyLibrary fancyLibrary = new FancyLibrary();

        for (int i = 0; i < 50; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    //fancyLibrary.changeNumber();
                    wrapLibrary(fancyLibrary);
                }
            }, "T" + i).start();
        }
    }

    private static synchronized void wrapLibrary(FancyLibrary checkAct) {
        checkAct.changeNumber();
    }
}