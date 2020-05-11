/////////////////////////////////////////////////////////////////////////////////////////
// 
//                 Université de Nice Sophia-Antipolis  (UNS) - 
//                 Centre National de la Recherche Scientifique (CNRS)
//                 Copyright © 2015 UNS, CNRS All Rights Reserved.
// 
//     These computer program listings and specifications, herein, are
//     the property of Université de Nice Sophia-Antipolis and CNRS
//     shall not be reproduced or copied or used in whole or in part as
//     the basis for manufacture or sale of items without written permission.
//     For a license agreement, please contact:
//     <mailto: licensing@sattse.com> 
//
//
//
//     Author: Luc Hogie – Laboratoire I3S - luc.hogie@unice.fr
//
//////////////////////////////////////////////////////////////////////////////////////////

 
 package octojus.gui.streams;

import java.awt.Color;
import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

public class StreamArea extends JPanel
{
	private JTextArea ta = new JTextArea();

	public StreamArea(String nodeName, String title)
	{
		setBorder(new TitledBorder("<html><i>" + nodeName + "</i>" + title));
		setLayout(new GridLayout(1, 1));
		add(new JScrollPane(ta));
		ta.setBackground(Color.black);
		ta.setForeground(Color.white);
	}

	public void appendText(String line)
	{
		Document d = ta.getDocument();

		try
		{
			d.insertString(d.getLength(), line, null);
		}
		catch (BadLocationException e)
		{
			throw new IllegalStateException(e);
		}
		
		ta.setCaretPosition(d.getLength());
	}
}