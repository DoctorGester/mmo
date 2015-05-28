import shared.board.Board
import shared.board.Buff
import shared.board.DamageType
import shared.board.Unit

def onInit(Buff buff, Board board){
    (buff.data as Unit).addBuff(buff)
}

def onTick(Buff buff, Board board){
    (buff.data as Unit).doDamage(1, DamageType.MAGIC)
}

def onEnd(Buff buff, Board board){
    (buff.data as Unit).removeBuff(buff)
}