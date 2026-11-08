package com.barrybecker4.sound.midi
/*
 * Extracted from the Sun Microsystems MidiSynth demo.
 * See MidiSynth.scala for the original copyright notice and license terms.
 */

import java.awt.event.{ActionEvent, ActionListener, ItemEvent, ItemListener}
import java.awt.Dimension
import javax.swing.*
import javax.swing.border.{EmptyBorder, EtchedBorder, TitledBorder}
import javax.swing.event.{ChangeEvent, ChangeListener}

/** A collection of MIDI controllers for [[MidiSynth]]. */
private[midi] class MidiControls(synth: MidiSynth)
    extends JPanel(),
      ActionListener,
      ChangeListener,
      ItemListener {

  {
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))
    setBorder(new EmptyBorder(5, 10, 5, 10))

    var p = new JPanel()
    p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS))
    synth.veloS = createSlider("Velocity", p)
    synth.presS = createSlider("Pressure", p)
    synth.revbS = createSlider("Reverb", p)
    synth.bendS = create14BitSlider("Bend", p)
    p.add(Box.createHorizontalStrut(10))
    add(p)

    p = new JPanel()
    p.setBorder(new EmptyBorder(10, 0, 10, 0))
    p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS))

    val combo = new JComboBox[String]()
    combo.setPreferredSize(new Dimension(120, 25))
    combo.setMaximumSize(new Dimension(120, 25))
    for (i <- 1 to 16) combo.addItem("Channel " + i)
    combo.addItemListener(this)
    p.add(combo)
    p.add(Box.createHorizontalStrut(20))

    synth.muteCB = createCheckBox("Mute", p)
    synth.soloCB = createCheckBox("Solo", p)
    synth.monoCB = createCheckBox("Mono", p)
    synth.sustCB = createCheckBox("Sustain", p)

    createButton("All Notes Off", p)
    p.add(Box.createHorizontalStrut(10))
    p.add(synth.mouseOverCB)
    p.add(Box.createHorizontalStrut(10))
    createButton("Record...", p)
    add(p)
  }

  private def createButton(name: String, p: JPanel): JButton = {
    val b = new JButton(name)
    b.addActionListener(this)
    p.add(b)
    b
  }

  private def createCheckBox(name: String, p: JPanel): JCheckBox = {
    val cb = new JCheckBox(name)
    cb.addItemListener(this)
    p.add(cb)
    cb
  }

  private def createSlider(name: String, p: JPanel): JSlider = {
    val slider = new JSlider(SwingConstants.HORIZONTAL, 0, 127, 64)
    slider.addChangeListener(this)
    val tb = new TitledBorder(new EtchedBorder())
    tb.setTitle(name + " = 64")
    slider.setBorder(tb)
    p.add(slider)
    p.add(Box.createHorizontalStrut(5))
    slider
  }

  private def create14BitSlider(name: String, p: JPanel): JSlider = {
    val slider = new JSlider(SwingConstants.HORIZONTAL, 0, 16383, 8192)
    slider.addChangeListener(this)
    val tb = new TitledBorder(new EtchedBorder())
    tb.setTitle(name + " = 8192")
    slider.setBorder(tb)
    p.add(slider)
    p.add(Box.createHorizontalStrut(5))
    slider
  }

  override def stateChanged(e: ChangeEvent): Unit = {
    val slider = e.getSource.asInstanceOf[JSlider]
    val value = slider.getValue
    val tb = slider.getBorder.asInstanceOf[TitledBorder]
    val s = tb.getTitle
    tb.setTitle(s.substring(0, s.indexOf('=') + 1) + value)
    val cc = synth.cc
    if (s.startsWith("Velocity")) cc.velocity = value
    else if (s.startsWith("Pressure")) cc.channel.setChannelPressure({ cc.pressure = value; value })
    else if (s.startsWith("Bend")) cc.channel.setPitchBend({ cc.bend = value; value })
    else if (s.startsWith("Reverb")) cc.channel.controlChange(MidiSynth.REVERB, { cc.reverb = value; value })
    slider.repaint()
  }

  override def itemStateChanged(e: ItemEvent): Unit = {
    e.getSource match
      case combo: JComboBox[?] =>
        synth.cc = synth.channels(combo.getSelectedIndex)
        setComponentStates(synth.cc)
      case cb: JCheckBox =>
        val name = cb.getText
        val cc = synth.cc
        if (name.startsWith("Mute")) cc.channel.setMute({ cc.mute = cb.isSelected; cb.isSelected })
        else if (name.startsWith("Solo")) cc.channel.setSolo({ cc.solo = cb.isSelected; cb.isSelected })
        else if (name.startsWith("Mono")) cc.channel.setMono({ cc.mono = cb.isSelected; cb.isSelected })
        else if (name.startsWith("Sustain")) {
          cc.sustain = cb.isSelected
          cc.channel.controlChange(MidiSynth.SUSTAIN, if (cc.sustain) 127 else 0)
        }
      case _ =>
  }

  private def setComponentStates(cc: ChannelData): Unit = {
    synth.table.setRowSelectionInterval(cc.row, cc.row)
    synth.table.setColumnSelectionInterval(cc.col, cc.col)
    synth.soloCB.setSelected(cc.solo)
    synth.monoCB.setSelected(cc.mono)
    synth.muteCB.setSelected(cc.mute)
    synth.sustCB.setSelected(cc.sustain)
    val sliders = Array(synth.veloS, synth.presS, synth.bendS, synth.revbS)
    val values = Array(cc.velocity, cc.pressure, cc.bend, cc.reverb)
    for (i <- sliders.indices) {
      val tb = sliders(i).getBorder.asInstanceOf[TitledBorder]
      val s = tb.getTitle
      tb.setTitle(s.substring(0, s.indexOf('=') + 1) + values(i))
      sliders(i).setValue(values(i))
      sliders(i).repaint()
    }
  }

  override def actionPerformed(e: ActionEvent): Unit = {
    val button = e.getSource.asInstanceOf[JButton]
    if (button.getText.startsWith("All")) {
      synth.channels.foreach(_.channel.allNotesOff())
      synth.keys.foreach(_.setNoteState(MidiSynth.OFF))
    } else if (button.getText.startsWith("Record")) {
      if (synth.recordFrame != null) synth.recordFrame.toFront()
      else synth.recordFrame = new MidiRecordFrame(synth)
    }
  }
}
