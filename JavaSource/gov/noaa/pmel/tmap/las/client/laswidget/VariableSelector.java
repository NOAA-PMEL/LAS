package gov.noaa.pmel.tmap.las.client.laswidget;

import gov.noaa.pmel.tmap.las.client.serializable.VariableSerializable;

import java.util.Vector;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.IsWidget;

/**
 * View base interface. Extends IsWidget so a view impl can easily provide its
 * container widget.
 */
public interface VariableSelector extends IsWidget {

    public interface Presenter {

        VariableSelector init(String id);
        
        UserListBox addUserListBox(UserListBox source, VariableSelector view);
        
        void itemCountUpdated(int oldItemCount, int newItemCount, VariableSelector view);

        void onAddButtonClick(ClickEvent event, UserListBox source, VariableSelector view);

        void onChange(ChangeEvent event, UserListBox newListBox);

        void onChange(ChangeEvent event, VariableSelector view);

        void onRemoveButtonClick(ClickEvent event, UserListBox source, VariableSelector view);

    }

    void addListBox(UserListBox newListBox);

    int getItemCount();
    
    UserListBox addUserListBox(UserListBox source, VariableSelector view);

    UserListBox getLatestListBox();

    Vector<UserListBox> getListBoxes();

    UserListBox initUserListBox(String id, boolean addChangeHandler);

    boolean isComparing();

    void removeListBox(UserListBox oldListBox);

    void removeListBoxesExceptFirst();

    void setComparing(boolean comparing);

    void setPresenter(Presenter listener);
    
    void setVariables(Vector<VariableSerializable> variables, int selected);

    void setVariableMetadataView(VariableMetadataView variableMetadataView);

    void removeExtraListBoxes(boolean update_count);

}
