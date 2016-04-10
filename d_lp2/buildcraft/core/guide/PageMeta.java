package buildcraft.core.guide;

public class PageMeta {
    public final String title;
    public final String customLocation;
    public final String customImageLocation;

    PageMeta(String title, String customLocation, String customImageLocation) {
        this.title = title;
        this.customLocation = customLocation;
        this.customImageLocation = customImageLocation;
    }

    public String[] getLocationArray() {
        if (customLocation == null) {
            return new String[0];
        }
        return customLocation.split("/");
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((customImageLocation == null) ? 0 : customImageLocation.hashCode());
        result = prime * result + ((title == null) ? 0 : title.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        PageMeta other = (PageMeta) obj;
        if (customImageLocation == null) {
            if (other.customImageLocation != null) {
                return false;
            }
        } else if (!customImageLocation.equals(other.customImageLocation)) {
            return false;
        }
        if (title == null) {
            if (other.title != null) {
                return false;
            }
        } else if (!title.equals(other.title)) {
            return false;
        }
        return true;
    }
}
