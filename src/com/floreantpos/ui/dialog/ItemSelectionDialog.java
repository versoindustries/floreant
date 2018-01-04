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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellRenderer;

import com.floreantpos.POSConstants;
import com.floreantpos.config.AppProperties;
import com.floreantpos.main.Application;
import com.floreantpos.model.dao.MenuItemDAO;
import com.floreantpos.swing.CheckBoxList;
import com.floreantpos.swing.CheckBoxList.Entry;
import com.floreantpos.swing.PosButton;
import com.floreantpos.ui.TitlePanel;
import com.floreantpos.util.POSUtil;

import net.miginfocom.swing.MigLayout;

public class ItemSelectionDialog extends POSDialog implements ActionListener {
	private TitlePanel titlePanel;
	private PosButton btnCancel;
	private PosButton btnOk;
	private JButton btnBack;
	private JButton btnForward;
	private JLabel lblNumberOfItem;

	private CheckBoxList cbListItems;
	private ItemCheckBoxListModel itemModel;
	private JTextField tfSearchField;
	//	private CheckBoxList cbSelectedItemList;
	//	private ItemCheckBoxListModel selectedItemModel;
	private PosButton btnAdd;
	//	private JPanel rightPanel;
	//	private TitlePanel leftTitlePanel;
	//	private TitlePanel rightTitlePanel;
	//	private JSeparator separator;
	private List<Entry> selectedItems = new ArrayList<>();
	private ButtonGroup btnGroup;
	private JCheckBox cbShowSelected;

	public ItemSelectionDialog() {
		super(POSUtil.getBackOfficeWindow(), true);
		setTitle(AppProperties.getAppName());
		init();
		initData();
	}

	private void initData() {
		String searchFieldTxt = tfSearchField.getText();
		btnGroup.clearSelection();
		cbShowSelected.setSelected(false);
		MenuItemDAO.getInstance().loadMenuItems(itemModel, searchFieldTxt);

		for (Entry<Entry> selectedEntry : selectedItems) {
			for (Entry<Entry> existedEntry : itemModel.getItems()) {
				if (existedEntry.getValue().equals(selectedEntry.getValue())) {
					existedEntry.setChecked(true);
				}
			}
		}

		int startNumber = itemModel.getCurrentRowIndex() + 1;
		int endNumber = itemModel.getNextRowIndex();
		int totalNumber = itemModel.getNumRows();
		if (endNumber > totalNumber) {
			endNumber = totalNumber;
		}
		lblNumberOfItem.setText(String.format("Showing %s to %s of %s", startNumber, endNumber, totalNumber));
		btnBack.setEnabled(itemModel.hasPrevious());
		btnForward.setEnabled(itemModel.hasNext());

		cbListItems.repaint();
	}

	private void init() {
		setLayout(new BorderLayout(10, 10));
		setIconImage(Application.getPosWindow().getIconImage());

		titlePanel = new TitlePanel();
		titlePanel.setTitle("Select item");
		add(titlePanel, BorderLayout.NORTH);

		JPanel centerPanel = new JPanel(new MigLayout("hidemode 3, fill", "", ""));
		JPanel leftPanel = new JPanel(new MigLayout("hidemode 3, fill, ins 0"));
		tfSearchField = new JTextField();
		tfSearchField.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				initData();
			}
		});
		PosButton btnSearch = new PosButton("Search");
		btnSearch.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				initData();
			}
		});
		btnBack = new JButton("<<< Previous");
		btnForward = new JButton("Next >>>");
		lblNumberOfItem = new JLabel();
		btnBack.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				itemModel.setCurrentRowIndex(itemModel.getPreviousRowIndex());
				initData();
			}
		});

		btnForward.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				itemModel.setCurrentRowIndex(itemModel.getNextRowIndex());
				initData();
			}
		});

		cbListItems = new CheckBoxList();
		String[] colHeaders = new String[] { "-", "Item name", "Item price" };
		itemModel = new ItemCheckBoxListModel(colHeaders);
		itemModel.setPageSize(20);
		cbListItems.setModel(itemModel);
		cbListItems.resizeColWidth();

		cbListItems.getColumnModel().getColumn(2).setCellRenderer(new TableCellRenderer() {

			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
				JLabel lblValue = new JLabel((String) value);
				lblValue.setHorizontalAlignment(JLabel.RIGHT);
				return lblValue;
			}
		});

		cbListItems.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				doSelectItem();
			}
		});

		cbShowSelected = new JCheckBox("Show selected items");
		btnGroup = new ButtonGroup();
		JCheckBox cbSelectAll = new JCheckBox("Select all");
		JCheckBox cbUnselectAll = new JCheckBox("Uncheck all");
		btnGroup.add(cbSelectAll);
		btnGroup.add(cbUnselectAll);
		cbShowSelected.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (cbShowSelected.isSelected()) {
					itemModel.setRows(selectedItems);
					cbListItems.selectAll();
					lblNumberOfItem.setText(String.format("Showing %s items", selectedItems.size()));
					btnBack.setEnabled(false);
					btnForward.setEnabled(false);
				}
				else {
					initData();
				}
			}
		});

		cbSelectAll.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				cbListItems.selectAll();
				List<Entry<Entry>> items = itemModel.getItems();
				if (items != null) {
					for (Entry entry : items) {
						addOrRemoveItem(entry);
					}
				}

			}
		});

		cbUnselectAll.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				cbListItems.unCheckAll();
				List<Entry<Entry>> items = itemModel.getItems();
				if (items != null) {
					for (Entry entry : items) {
						addOrRemoveItem(entry);
					}
				}
			}
		});

		leftPanel.add(tfSearchField, "span, grow,split 2");
		leftPanel.add(btnSearch, "wrap");
		leftPanel.add(new JScrollPane(cbListItems), "span, grow,wrap");

		leftPanel.add(cbShowSelected, "left, split 3");
		leftPanel.add(cbSelectAll);
		leftPanel.add(cbUnselectAll);

		leftPanel.add(lblNumberOfItem, "right, split 3");
		leftPanel.add(btnBack);
		leftPanel.add(btnForward);

		btnAdd = new PosButton("ADD");
		btnAdd.setFocusable(false);
		btnAdd.addActionListener(this);

		btnOk = new PosButton(POSConstants.OK);
		btnOk.setFocusable(false);
		btnOk.addActionListener(this);

		btnCancel = new PosButton(POSConstants.CANCEL);
		btnCancel.setFocusable(false);
		btnCancel.addActionListener(this);

		JPanel footerPanel = new JPanel(new MigLayout("ins 2, center", "", ""));
		footerPanel.add(btnOk, "w 100!, center, split 2");
		footerPanel.add(btnCancel, "w 100!");

		add(footerPanel, BorderLayout.SOUTH);

		centerPanel.add(leftPanel, "grow");

		add(centerPanel, BorderLayout.CENTER);

		setSize(900, 500);
	}

	private void doOk() {
		setCanceled(false);
		dispose();
	}

	private void doCancel() {
		setCanceled(true);
		dispose();
	}

	public void actionPerformed(ActionEvent e) {
		String actionCommand = e.getActionCommand();

		if (POSConstants.CANCEL.equalsIgnoreCase(actionCommand)) {
			doCancel();
		}
		else if (POSConstants.OK.equalsIgnoreCase(actionCommand)) {
			doOk();
		}
	}

	public List<Entry> getItems() {
		return selectedItems;
	}

	public void setItems(List<Entry<Entry>> list) {
		if (list == null) {
			return;
		}
		for (Entry entry : list) {
			selectedItems.add(entry);
		}
		initData();
	}

	private void doSelectItem() {
		try {
			btnGroup.clearSelection();
			int selectedRow = cbListItems.getSelectedRow();
			if (selectedRow < 0) {
				return;
			}
			selectedRow = cbListItems.convertRowIndexToModel(selectedRow);

			Entry entry = itemModel.getRow(selectedRow);
			addOrRemoveItem(entry);
		} catch (Exception ex) {
			POSMessageDialog.showError(POSUtil.getFocusedWindow(), ex.getMessage(), ex);
		}
	}

	private void addOrRemoveItem(Entry entry) {
		if (entry.isChecked()) {
			selectedItems.add(entry);
		}
		else {
			for (Iterator iterator = selectedItems.iterator(); iterator.hasNext();) {
				Entry existedEntry = (Entry) iterator.next();
				if (entry.getValue().equals(existedEntry.getValue())) {
					iterator.remove();
				}
			}
		}
	}

}
