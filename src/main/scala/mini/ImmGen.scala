// See LICENSE for license details.

package mini

import chisel3._
import chisel3.util._
import mini.Control._

// imm_Gen input and output
class ImmGenIO(xlen: Int) extends Bundle {
  val inst = Input(UInt(xlen.W))  // instrcution form IR(instruction register)
  val sel = Input(UInt(3.W))      // imm_sel from datapath.ctrl
  val out = Output(UInt(xlen.W))
}

// imm_Gen IO + parameter xlen
trait ImmGen extends Module {
  def xlen: Int
  val io: ImmGenIO
}

// six input mux, 'io.sel' selects one input of them as ouput
class ImmGenWire(val xlen: Int) extends ImmGen {
  val io = IO(new ImmGenIO(xlen))   // instantiate ImmGenIO
  val Iimm = io.inst(31, 20).asSInt // I-type // asSInt is sign bit extend 
  val Simm = Cat(io.inst(31, 25), io.inst(11, 7)).asSInt // S-type
  val Bimm = Cat(io.inst(31), io.inst(7), io.inst(30, 25), io.inst(11, 8), 0.U(1.W)).asSInt // B-type
  val Uimm = Cat(io.inst(31, 12), 0.U(12.W)).asSInt // U-type
  val Jimm = Cat(io.inst(31), io.inst(19, 12), io.inst(20), io.inst(30, 25), io.inst(24, 21), 0.U(1.W)).asSInt // J-type
  val Zimm = io.inst(19, 15).zext // get uimm[4:0] and zero extend, {27'b0, inst[19:15]}

  io.out := MuxLookup(io.sel, Iimm & (-2).S)(
    Seq(IMM_I -> Iimm, 
        IMM_S -> Simm, 
        IMM_B -> Bimm, 
        IMM_U -> Uimm, 
        IMM_J -> Jimm, 
        IMM_Z -> Zimm )
  ).asUInt
}

// separately generate six type of imms use some mux, then put them together and use sign bit to extend  
class ImmGenMux(val xlen: Int) extends ImmGen {
  val io = IO(new ImmGenIO(xlen))
  val sign = Mux(io.sel === IMM_Z, 0.S, io.inst(31).asSInt)
  val b30_20 = Mux(io.sel === IMM_U, io.inst(30, 20).asSInt, sign)
  val b19_12 = Mux(io.sel =/= IMM_U && io.sel =/= IMM_J, sign, io.inst(19, 12).asSInt)
  val b11 = Mux(
    io.sel === IMM_U || io.sel === IMM_Z,
    0.S,
    Mux(io.sel === IMM_J, io.inst(20).asSInt, Mux(io.sel === IMM_B, io.inst(7).asSInt, sign))
  )
  val b10_5 = Mux(io.sel === IMM_U || io.sel === IMM_Z, 0.U, io.inst(30, 25))
  val b4_1 = Mux(
    io.sel === IMM_U,
    0.U,
    Mux(io.sel === IMM_S || io.sel === IMM_B, io.inst(11, 8), Mux(io.sel === IMM_Z, io.inst(19, 16), io.inst(24, 21)))
  )
  val b0 =
    Mux(io.sel === IMM_S, io.inst(7), Mux(io.sel === IMM_I, io.inst(20), Mux(io.sel === IMM_Z, io.inst(15), 0.U)))

  io.out := Cat(sign, b30_20, b19_12, b11, b10_5, b4_1, b0).asSInt.asUInt
}
