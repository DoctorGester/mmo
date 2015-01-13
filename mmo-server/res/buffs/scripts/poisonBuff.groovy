import core.board.DamageType
import core.board.interfaces.Board
import core.board.interfaces.Buff
import core.board.interfaces.Unit

def onInit(Buff buff, Board board){
    (buff.data as Unit).addBuff(buff)
}

def onTick(Buff buff, Board board){
    (buff.data as Unit).doDamage(1, DamageType.SPELL)
}

def onEnd(Buff buff, Board board){
    (buff.data as Unit).removeBuff(buff)
}