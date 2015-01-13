import core.board.Board
import core.board.Buff
import core.board.ControlType
import core.board.Unit

def onInit(Buff buff, Board board){
    if (buff.data instanceof Unit) {
        (buff.data as Unit).addBuff(buff)
        (buff.data as Unit).applyControl(ControlType.SILENCE, buff)
    } else if (buff.data instanceof List<Unit>){
        for (Unit unit in (buff.data as List<Unit>)){
            unit.addBuff(buff)
            unit.applyControl(ControlType.SILENCE, buff)
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