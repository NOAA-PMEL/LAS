/**
 * 
 */
package gov.noaa.pmel.tmap.las.client.laswidget;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.CustomButton;

/**
 * This class was necessary in order to have a toggle button that didn't have a
 * darkened look when in its down state that the default GWT 2.4
 * {@link com.google.gwt.user.client.ui.ToggleButton} does .
 * 
 * @author weusijana
 */
public class PlainToggleButton extends CustomButton {
	// Copied from com.google.gwt.user.client.ui.PushButton
	private static final String STYLENAME_DEFAULT = "gwt-PushButton";
	private String downText = "Do the opposite";
	private String upText = "Do it";
	private boolean isDown = false;
	ClickHandler toggleOnClickHandler = new ClickHandler() {
		@Override
		public void onClick(ClickEvent event) {
			setDown(!isDown());
		}
	};

	/**
	 * 
	 */
	public PlainToggleButton() {
		super();
		init();
	}

	/**
	 * @param upText
	 */
	public PlainToggleButton(String upText) {
		super(upText);
		this.upText = upText;
		init();
	}

	/**
	 * @param upText
	 * @param handler
	 */
	public PlainToggleButton(String upText, ClickHandler handler) {
		super(upText);
		this.upText = upText;
		init();
		this.addClickHandler(handler);
	}

	/**
	 * @param upText
	 * @param downText
	 */
	public PlainToggleButton(String upText, String downText) {
		super(upText);
		this.upText = upText;
		this.downText = downText;
		init();
	}

	/**
	 * @param upText
	 * @param downText
	 * @param handler
	 */
	public PlainToggleButton(String upText, String downText,
			ClickHandler handler) {
		super(upText);
		this.upText = upText;
		this.downText = downText;
		init();
		this.addClickHandler(handler);
	}

	/**
	 * This initialization method must be called before adding any handlers to
	 * insure that the {@link toggleOnClickHandler} field gets added first.
	 */
	private void init() {
		// Copied from com.google.gwt.user.client.ui.CustomButton
		setStyleName(STYLENAME_DEFAULT);
		
		addClickHandler(toggleOnClickHandler);
	}

	// public HandlerRegistration addValueChangeHandler(
	// ValueChangeHandler<Boolean> handler) {
	// // TODO Auto-generated method stub
	// return super.addValueChangeHandler(handler);
	// }
	//
	// public Boolean getValue() {
	// // TODO Auto-generated method stub
	// return super.getValue();
	// }

	@Override
	public boolean isDown() {
		return isDown;
	}

	/**
	 * This method is overridden to prevent changes in this button's text due to
	 * hovering.
	 * 
	 * @see com.google.gwt.user.client.ui.CustomButton#onBrowserEvent(com.google.gwt.user.client.Event)
	 */
	@Override
	public void onBrowserEvent(Event event) {
		String originalText = getText();
		super.onBrowserEvent(event);
		// Should not act on button if disabled.
		if (isEnabled() == false) {
			// This can happen when events are bubbled up from non-disabled
			// children
			return;
		}

		int type = DOM.eventGetType(event);
		switch (type) {
		// case Event.ONCLICK:
		// // If clicks are currently disallowed, keep it from bubbling or being
		// // passed to the superclass.
		// if (!allowClick) {
		// event.stopPropagation();
		// return;
		// }
		// break;
		// case Event.ONMOUSEDOWN:
		// if (event.getButton() == Event.BUTTON_LEFT) {
		// setFocus(true);
		// onClickStart();
		// DOM.setCapture(getElement());
		// isCapturing = true;
		// // Prevent dragging (on some browsers);
		// DOM.eventPreventDefault(event);
		// }
		// break;
		// case Event.ONMOUSEUP:
		// if (isCapturing) {
		// isCapturing = false;
		// DOM.releaseCapture(getElement());
		// if (isHovering() && event.getButton() == Event.BUTTON_LEFT) {
		// onClick();
		// }
		// }
		// break;
		// case Event.ONMOUSEMOVE:
		// if (isCapturing) {
		// // Prevent dragging (on other browsers);
		// DOM.eventPreventDefault(event);
		// }
		// break;
		case Event.ONMOUSEOUT:
			// Element to = DOM.eventGetToElement(event);
			// if (DOM.isOrHasChild(getElement(), DOM.eventGetTarget(event))
			// && (to == null || !DOM.isOrHasChild(getElement(), to))) {
			// if (isCapturing) {
			// onClickCancel();
			// }
			// setHovering(false);
			// }

			// Undo any changes superclass may have caused due to hovering.
			setText(originalText);
			break;
		case Event.ONMOUSEOVER:
			// if (DOM.isOrHasChild(getElement(), DOM.eventGetTarget(event))) {
			// setHovering(true);
			// if (isCapturing) {
			// onClickStart();
			// }
			// }

			// Undo any changes superclass may have caused due to hovering.
			setText(originalText);
			break;
		// case Event.ONBLUR:
		// if (isFocusing) {
		// isFocusing = false;
		// onClickCancel();
		// }
		// break;
		// case Event.ONLOSECAPTURE:
		// if (isCapturing) {
		// isCapturing = false;
		// onClickCancel();
		// }
		// break;
		}

		// super.onBrowserEvent(event);

		// Synthesize clicks based on keyboard events AFTER the normal key
		// handling.
		// if ((event.getTypeInt() & Event.KEYEVENTS) != 0) {
		// char keyCode = (char) DOM.eventGetKeyCode(event);
		// switch (type) {
		// case Event.ONKEYDOWN:
		// if (keyCode == ' ') {
		// isFocusing = true;
		// onClickStart();
		// }
		// break;
		// case Event.ONKEYUP:
		// if (isFocusing && keyCode == ' ') {
		// isFocusing = false;
		// onClick();
		// }
		// break;
		// case Event.ONKEYPRESS:
		// if (keyCode == '\n' || keyCode == '\r') {
		// onClickStart();
		// onClick();
		// }
		// break;
		// }
		// }
	}

	// public void setValue(Boolean value) {
	// // TODO Auto-generated method stub
	// super.setValue(value);
	// }
	//
	// public void setValue(Boolean value, boolean fireEvents) {
	// // TODO Auto-generated method stub
	// super.setValue(value, fireEvents);
	// }
	//
	// protected void onClick() {
	// // TODO Auto-generated method stub
	// super.onClick();
	// }

	@Override
	public void setDown(boolean down) {
		isDown = down;
		if (down) {
			setText(downText);
		} else {
			setText(upText);
		}
	}

}
