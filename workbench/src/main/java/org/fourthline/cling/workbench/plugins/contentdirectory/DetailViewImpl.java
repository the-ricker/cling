/*
 * Copyright (C) 2011 4th Line GmbH, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.fourthline.cling.workbench.plugins.contentdirectory;

import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.support.model.ProtocolInfo;
import org.fourthline.cling.support.model.Res;
import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.item.Item;

import javax.annotation.PostConstruct;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Christian Bauer
 */
public class DetailViewImpl extends JPanel implements DetailView {

    protected Presenter presenter;

    @PostConstruct
    public void init() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(300, 100));
    }

    @Override
    public Component asUIComponent() {
        return this;
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;

        removeAll();
        JLabel welcomeLabel = new JLabel("Please select an item.");
        welcomeLabel.setBorder(new EmptyBorder(20, 20, 20, 20));
        add(welcomeLabel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    @Override
    public void showContainer(Container container) {
        removeAll();

        JScrollPane scrollPane = new JScrollPane(new ContainerFormPanel(container));
        add(scrollPane, BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    @Override
    public void showItem(Item item, final Map<Device, List<ProtocolInfo>> mediaRenderers) {
        removeAll();

        ItemFormPanel itemFormPanel = new ItemFormPanel(item) {

            public List<JMenuItem> createSendToMenuItems(final Res resource) {
                List<JMenuItem> menuItems = new ArrayList();

                for (Map.Entry<Device, List<ProtocolInfo>> entry : mediaRenderers.entrySet()) {

                    final Service avTransportService =
                            presenter.getMatchingAVTransportService(entry.getKey(), entry.getValue(), resource);

                    JMenuItem menuItem;

                    if (avTransportService != null) {

                        menuItem = new JMenu(entry.getKey().getDetails().getFriendlyName());
                        for (int i = 0; i < SUPPORTED_INSTANCES; i++) {
                            final int instanceId = i;
                            JMenuItem instanceItem = new JMenuItem("Instance: " + i);
                            instanceItem.addActionListener(new ActionListener() {
                                public void actionPerformed(ActionEvent e) {
                                    presenter.onSendToMediaRenderer(
                                            instanceId, avTransportService, resource.getValue()
                                    );
                                }
                            });
                            menuItem.add(instanceItem);
                        }

                    } else {

                        menuItem = new JMenuItem(
                                entry.getKey().getDetails().getFriendlyName() + " (Not Compatible)"
                        );
                        menuItem.setEnabled(false);

                    }

                    menuItems.add(menuItem);
                }


                if (menuItems.size() == 0) {
                    JMenuItem noRenderersItem = new JMenuItem("No MediaRenderers found...");
                    noRenderersItem.setEnabled(false);
                    menuItems.add(noRenderersItem);
                }

                return menuItems;
            }
        };
        JScrollPane scrollPane = new JScrollPane(itemFormPanel);
        add(scrollPane, BorderLayout.CENTER);

        revalidate();
        repaint();
    }

}
