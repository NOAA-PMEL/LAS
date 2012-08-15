/*******************************************************************************
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package gov.noaa.pmel.tmap.las.client;

import gov.noaa.pmel.tmap.las.client.activity.HelpMenuBarActivity;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;

public class HelpMenu implements EntryPoint {

    private SimplePanel appWidget = new SimplePanel();

    @Override
    public void onModuleLoad() {
        // Create ClientFactory using deferred binding so we can replace with
        // different impls in gwt.xml
        ClientFactory clientFactory = GWT.create(ClientFactory.class);
        EventBus eventBus = clientFactory.getEventBus();
        RootPanel.get("headerBottomRight").add(appWidget);

        HelpMenuBarActivity helpMenuBarPresenter = new HelpMenuBarActivity(clientFactory);
        appWidget.setWidth("55px");
        appWidget.ensureDebugId("helpMenuPanel");
        helpMenuBarPresenter.start(appWidget, eventBus);
    }
}
