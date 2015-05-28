import shared.board.Board
import shared.board.Buff
import shared.board.Unit

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