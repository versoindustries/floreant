/**
 * ************************************************************************
 * * The contents of this file are subject to the MRPL 1.2
 * * (the  "License"),  being   the  Mozilla   Public  License
 * * Version 1.1  with a permitted attribution clause; you may not  use this
 * * file except in compliance with the License. You  may  obtain  a copy of
 * * the License at http://www.floreantpos.org/license.html
 * * Software distributed under the License  is  distributed  on  an "AS IS"
 * * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * * License for the specific  language  governing  rights  and  limitations
 * * under the License.
 * * The Original Code is FLOREANT POS.
 * * The Initial Developer of the Original Code is OROCUBE LLC
 * * All portions are Copyright (C) 2015 OROCUBE LLC
 * * All Rights Reserved.
 * ************************************************************************
 */
package com.floreantpos.ui.dialog;

import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.miginfocom.swing.MigLayout;

import com.floreantpos.POSConstants;
import com.floreantpos.main.Application;
import com.floreantpos.model.MenuItem;
import com.floreantpos.model.dao.MenuItemDAO;
import com.floreantpos.swing.BeanTableModel;
import com.floreantpos.swing.PosButton;
import com.floreantpos.swing.PosScrollPane;
import com.floreantpos.swing.PosUIManager;
import com.floreantpos.swing.QwertyKeyPad;
import com.floreantpos.util.POSUtil;

public class ItemSearchDialog extends OkCancelOptionDialog {

	private JTextField tfNumber;
	private JTable table;
	private BeanTableModel<MenuItem> tableModel;
	private MenuItem selectedItem;

	public ItemSearchDialog() {
		super(POSUtil.getFocusedWindow(), POSConstants.SEARCH_ITEM_BUTTON_TEXT);
		init();
	}

	public ItemSearchDialog(Frame parent) {
		super(parent, POSConstants.SEARCH_ITEM_BUTTON_TEXT);
		init();
	}

	private void init() {
		setResizable(false);

		JPanel contentPane = getContentPanel();
		contentPane.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));

		MigLayout layout = new MigLayout("inset 0"); //$NON-NLS-1$ 
		contentPane.setLayout(layout);

		tfNumber = new JTextField();
		tfNumber.setFont(tfNumber.getFont().deriveFont(Font.BOLD, PosUIManager.getNumberFieldFontSize()));
		tfNumber.setFocusable(true);
		tfNumber.requestFocus();
		tfNumber.setBackground(Color.WHITE);

		ActionListener searchListener = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String searchString = tfNumber.getText();
				if (searchString.equals("0") || searchString.equals("")) {
					POSMessageDialog.showError(Application.getPosWindow(), "Please enter barcode or item no or name.");
					return;
				}
				List<MenuItem> menuItems = new ArrayList<>();
				MenuItem menuItem = MenuItemDAO.getInstance().getMenuItemByBarcode(searchString);
				if (menuItem == null) {
					try {
						Integer id = Integer.valueOf(searchString);
						menuItem = MenuItemDAO.getInstance().get(id);
					} catch (Exception e2) {
					}
				}
				if (menuItem == null) {
					menuItems = MenuItemDAO.getInstance().getMenuItemByName(searchString);
				}
				if (menuItem != null) {
					menuItems.add(menuItem);
				}
				if ((menuItems != null && menuItems.size() == 1)) {
					selectedItem = menuItems.get(0);
					doOk();
				}
				else {
					tableModel.setRows(menuItems);
				}
			}
		};
		tfNumber.addActionListener(searchListener);

		PosButton btnSearch = new PosButton(POSConstants.SEARCH);
		btnSearch.addActionListener(searchListener);

		contentPane.add(tfNumber, "spanx,split 2, grow"); //$NON-NLS-1$
		contentPane.add(btnSearch, "w 90!");

		PosScrollPane scrollPane = new PosScrollPane();
		table = new JTable();
		table.setRowHeight(35);
		table.getTableHeader().setPreferredSize(PosUIManager.getSize(0, 0));
		scrollPane.setViewportView(table);

		tableModel = new BeanTableModel<MenuItem>(MenuItem.class);
		tableModel.addColumn(POSConstants.NAME, MenuItem.PROP_NAME);

		table.setModel(tableModel);
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				int index = table.getSelectedRow();
				if (index < 0)
					return;
				index = table.convertRowIndexToModel(index);
				selectedItem = tableModel.getRow(index);
			}
		});

		QwertyKeyPad qwertyKeyPad = new QwertyKeyPad();

		contentPane.add(scrollPane, "spanx,grow,h 170!,w 720!"); //$NON-NLS-1$
		contentPane.add(qwertyKeyPad, "spanx ,grow"); //$NON-NLS-1$
	}

	@Override
	public void doOk() {
		setCanceled(false);
		dispose();
	}

	public MenuItem getSelectedItem() {
		return selectedItem;
	}
}
