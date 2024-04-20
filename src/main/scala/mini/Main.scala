// See LICENSE for license details.

package mini

import circt.stage.ChiselStage

object Main extends App {
  val config = MiniConfig() // get config parameters for Tile instantiate 
  ChiselStage.emitSystemVerilogFile( // generate Systeam Verilog file: Tile.sv
    new Tile( // the Top is Tile
      coreParams = config.core,
      nastiParams = config.nasti,
      cacheParams = config.cache
    ),
    args
  )
}
