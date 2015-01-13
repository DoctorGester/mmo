import core.board.Board
import core.board.Buff
import core.board.ControlType
import core.board.Unit

def onInit(Buff buff, Board board){
    if (buff.data instanceof Unit) {
        (buff.data as Unit).addBuff(buff)
        (buff.data as Unit).applyControl(ControlType.STUN, buff)
    } else if (buff.data instanceof List<Unit>){
        for (Unit unit in (buff.data as List<Unit>)){
            unit.addBuff(buff)
            unit.applyControl(ControlType.STUN, buff)
        }
    }
}

def onTick(Buff buff, Board board){}

def onEnd(Buff buff, Board board){
    if (buff.data instanceof Unit) {
        (buff.data as Unit).removeBuff(buff)
    } else if (buff.data instanceof List<Unit>){

        for (Unit unit in (buff.data as List<Unit>))
            unit.removeBuff(buff)

    }

}