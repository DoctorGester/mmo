import core.board.Board
import core.board.Buff
import core.board.Unit
import core.board.interfaces.Board
import core.board.interfaces.Buff
import core.board.interfaces.Unit

def onInit(Buff buff, Board board){
    Unit unit = (buff.data["unit"] as Unit);

    unit.addBuff(buff)
    unit.bonusAttackDamage += buff.data["value"]
}

def onTick(Buff buff, Board board){
}

def onEnd(Buff buff, Board board){
    Unit unit = (buff.data["unit"] as Unit);

    unit.removeBuff(buff)
    unit.bonusAttackDamage -= buff.data["value"]
}