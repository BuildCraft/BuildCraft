package codechicken.nei;

public class VisiblityData
{
    public boolean showUtilityButtons = true;
    public boolean showStateButtons = true;
    /**
     * Item panel and associated buttons
     */
    public boolean showItemPanel = true;
    /**
     * Item and search section
     */
    public boolean showItemSection = true;
    /**
     * Dropdown and Item search field
     */
    public boolean showSearchSection = true;
    /**
     * All widgets except options button
     */
    public boolean showWidgets = true;
    /**
     * The entire NEI interface, aka hidden
     */
    public boolean showNEI = true;
    public boolean enableDeleteMode = true;
    
    public void translateDependancies()
    {
        if(!showNEI)
            showWidgets = false;
        if(!showWidgets)
            showItemSection = showUtilityButtons = showStateButtons = false;
        if(!showItemSection)
            showSearchSection = showItemPanel = false;
    }
}