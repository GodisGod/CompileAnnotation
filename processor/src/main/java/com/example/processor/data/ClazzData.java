package com.example.processor.data;

/**
 * author: liutao
 * date: 2016/6/17.
 */
public class ClazzData {
    private boolean isAbstract;
    private String pageName;
    private String simpleName;
    private String bundleName;
    public void setAbstract(boolean anAbstract) {
        isAbstract = anAbstract;
    }

    public void setPageName(String pageName) {
        this.pageName = pageName;
    }

    public boolean isAbstract() {
        return isAbstract;
    }

    public String getPageName() {
        return pageName;
    }

    public void setSimpleName(String simpleName) {
        this.simpleName = simpleName;
    }

    public String getSimpleName() {
        return simpleName;
    }

    public void setBundleName(String bundleName) {
        this.bundleName = bundleName;
    }

    public String getBundleName() {
        return bundleName;
    }
}
