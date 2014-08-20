package ro.rcsrds.recordbox;

import net.koofr.api.v2.transfer.ProgressListener;


class SimpleProgressListener implements ProgressListener  {
    private long total = 0;

    public void transferred(long bytes) {
        setTotal(total + bytes);
    }

    public void setTotal(long bytes) {
        this.total = bytes;
        //does not work; shows wrong progress
        //Log.d("Filename","Progress: "+bytes+" bytes");
    }

    public boolean isCanceled() {
        return false;
    }
}