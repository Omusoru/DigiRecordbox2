package ro.rcsrds.recordbox;

import net.koofr.api.v2.transfer.ProgressListener;


class SimpleProgressListener implements ProgressListener  {
    private long total = 0;
    private boolean canceled = false;

    public void transferred(long bytes) {
        //setTotal(total + bytes);
    	setTotal(bytes);
    }

    public void setTotal(long bytes) {
        this.total = bytes;
    	//Log.d("Progress","Progress: " + ((double)bytes)/(1000*1000) + " bytes\r");
    }

    public boolean isCanceled() {
        return canceled;
    }
    
    public void cancel() {
    	this.canceled = true;
    }
    
    public long getTotal() {
    	return total;
    }
}