import core.board.Board
import core.board.Buff
import core.board.DamageType
import core.board.Unit

def onInit(Buff buff, Board board){
    (buff.data as Unit).addBuff(buff)
}

def onTick(Buff buff, Board board){
    (buff.data as Unit).doDamage(1, DamageType.SPELL)
}

def onEnd(Buff buff, Board board){
    (buff.data as Unit).removeBuff(buff)
}