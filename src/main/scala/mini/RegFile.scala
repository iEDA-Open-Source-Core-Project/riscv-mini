// See LICENSE for license details.

package mini
import chisel3._

// RegFile Module Input and Output
class RegFileIO(xlen: Int) extends Bundle {
  // up to two operands can be read in one cycle, combinational/asynchronous-read
  // rs1 
  val raddr1 = Input(UInt(5.W))
  val raddr2 = Input(UInt(5.W))
  // rs2
  val rdata1 = Output(UInt(xlen.W))
  val rdata2 = Output(UInt(xlen.W))
  // rd
  val wen = Input(Bool())
  val waddr = Input(UInt(5.W))
  val wdata = Input(UInt(xlen.W))
}

// RegFile Module
class RegFile(xlen: Int) extends Module {
  // instantiate RegFileIO
  val io = IO(new RegFileIO(xlen))
  // A combinational/asynchronous-read, sequential/synchronous-write memory.
  val regs = Mem(32, UInt(xlen.W)) // x0, ..., x31, x0 always is zero 
  // read data logic
  io.rdata1 := Mux(io.raddr1.orR, regs(io.raddr1), 0.U) // rdata1 = (addr1 != 0) ? regs[raddr1] : 0
  io.rdata2 := Mux(io.raddr2.orR, regs(io.raddr2), 0.U) // rdata2 = (addr2 != 0) ? regs[raddr2] : 0
  // write data logic
  when(io.wen & io.waddr.orR) { // x0 is not writable
    regs(io.waddr) := io.wdata
  }
}
